<template>
  <div class="stock-list-enhanced">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2>股票名录</h2>
      <p class="page-desc">支持按市场、板块筛选的股票列表查询</p>
    </div>

    <!-- 筛选条件区域 -->
    <el-card class="filter-card">
      <div slot="header" class="card-header">
        <span>筛选条件</span>
        <el-button type="primary" size="mini" @click="loadAllData">
          <i class="el-icon-refresh"></i> 刷新全部
        </el-button>
      </div>
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <!-- 市场筛选 -->
        <el-form-item label="所属市场">
          <el-select
            v-model="filterForm.market"
            placeholder="请选择市场"
            clearable
            @change="handleMarketChange"
          >
            <el-option label="全部市场" value=""></el-option>
            <el-option label="沪市" value="sh">
              <span style="float: left">沪市</span>
              <span style="float: right; color: #8492a6; font-size: 13px">上海证券交易所</span>
            </el-option>
            <el-option label="深市" value="sz">
              <span style="float: left">深市</span>
              <span style="float: right; color: #8492a6; font-size: 13px">深圳证券交易所</span>
            </el-option>
            <el-option label="北交所" value="bj">
              <span style="float: left">北交所</span>
              <span style="float: right; color: #8492a6; font-size: 13px">北京证券交易所</span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 板块筛选 -->
        <el-form-item label="所属板块">
          <el-select
            v-model="filterForm.board"
            placeholder="请选择板块"
            clearable
            :disabled="!filterForm.market"
          >
            <el-option label="全部板块" value=""></el-option>
            <el-option
              v-for="item in boardOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            ></el-option>
          </el-select>
        </el-form-item>

        <!-- 搜索输入 -->
        <el-form-item label="股票搜索">
          <el-input
            v-model="filterForm.searchText"
            placeholder="输入代码或名称"
            prefix-icon="el-icon-search"
            clearable
            style="width: 200px"
          ></el-input>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <i class="el-icon-search"></i> 查询
          </el-button>
          <el-button @click="handleReset">
            <i class="el-icon-refresh-left"></i> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 统计信息 -->
      <div class="statistics-bar">
        <el-tag type="info" effect="plain">
          <i class="el-icon-s-data"></i>
          共 {{ filteredData.length }} 只股票
        </el-tag>
        <el-tag v-if="filterForm.market" type="success" effect="plain" style="margin-left: 10px">
          {{ getMarketLabel(filterForm.market) }}
        </el-tag>
        <el-tag v-if="filterForm.board" type="warning" effect="plain" style="margin-left: 10px">
          {{ getBoardLabel(filterForm.board) }}
        </el-tag>
      </div>
    </el-card>

    <!-- 股票列表表格 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>股票列表</span>
        <div class="header-actions">
          <el-button type="success" size="mini" @click="exportData">
            <i class="el-icon-download"></i> 导出
          </el-button>
        </div>
      </div>

      <el-table
        :data="paginatedData"
        style="width: 100%"
        stripe
        border
        v-loading="loading"
        height="600"
        @sort-change="handleSortChange"
      >
        <!-- 股票代码 -->
        <el-table-column
          prop="代码"
          label="股票代码"
          min-width="100"
          sortable
          fixed
        >
          <template slot-scope="scope">
            <el-tag
              :type="getMarketTagType(scope.row['市场'])"
              size="small"
              effect="plain"
            >
              {{ scope.row['代码'] }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 股票名称 -->
        <el-table-column
          prop="名称"
          label="股票名称"
          min-width="120"
          sortable
          fixed
        >
          <template slot-scope="scope">
            <span class="stock-name">{{ scope.row['名称'] }}</span>
          </template>
        </el-table-column>

        <!-- 所属市场 -->
        <el-table-column
          prop="市场"
          label="所属市场"
          min-width="100"
          sortable
        >
          <template slot-scope="scope">
            <el-tag :type="getMarketTagType(scope.row['市场'])" size="small">
              {{ getMarketLabel(scope.row['市场']) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 所属板块 -->
        <el-table-column
          prop="板块"
          label="所属板块"
          min-width="100"
          sortable
        >
          <template slot-scope="scope">
            <el-tag
              :type="getBoardTagType(scope.row['板块'])"
              size="small"
              effect="plain"
            >
              {{ scope.row['板块'] || '-' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 上市日期 -->
        <el-table-column
          prop="上市日期"
          label="上市日期"
          min-width="120"
          sortable
        >
          <template slot-scope="scope">
            <i class="el-icon-date"></i>
            {{ formatDate(scope.row['上市日期']) }}
          </template>
        </el-table-column>

        <!-- 总股本 -->
        <el-table-column
          prop="总股本"
          label="总股本(亿股)"
          min-width="130"
          sortable
          align="right"
        >
          <template slot-scope="scope">
            <span class="number-cell">{{ formatNumber(scope.row['总股本']) }}</span>
          </template>
        </el-table-column>

        <!-- 流通股本 -->
        <el-table-column
          prop="流通股本"
          label="流通股本(亿股)"
          min-width="130"
          sortable
          align="right"
        >
          <template slot-scope="scope">
            <span class="number-cell">{{ formatNumber(scope.row['流通股本']) }}</span>
          </template>
        </el-table-column>

        <!-- 所属行业 -->
        <el-table-column
          prop="所属行业"
          label="所属行业"
          min-width="150"
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <el-tag
              v-if="scope.row['所属行业']"
              type="info"
              size="small"
              effect="plain"
            >
              {{ scope.row['所属行业'] }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          min-width="150"
          fixed="right"
          align="center"
        >
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="viewHistory(scope.row)"
            >
              <i class="el-icon-trend"></i> K线
            </el-button>
            <el-button
              size="mini"
              type="info"
              @click="viewDetail(scope.row)"
            >
              <i class="el-icon-document"></i> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-wrapper">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="pagination.currentPage"
          :page-sizes="[20, 50, 100, 200]"
          :page-size="pagination.pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="filteredData.length"
        ></el-pagination>
      </div>
    </el-card>

    <!-- 股票详情弹窗 -->
    <el-dialog
      title="股票详情"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <el-descriptions :column="2" border v-if="selectedStock">
        <el-descriptions-item label="股票代码">
          {{ selectedStock['代码'] }}
        </el-descriptions-item>
        <el-descriptions-item label="股票名称">
          {{ selectedStock['名称'] }}
        </el-descriptions-item>
        <el-descriptions-item label="所属市场">
          <el-tag :type="getMarketTagType(selectedStock['市场'])" size="small">
            {{ getMarketLabel(selectedStock['市场']) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="所属板块">
          <el-tag :type="getBoardTagType(selectedStock['板块'])" size="small">
            {{ selectedStock['板块'] || '-' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上市日期">
          {{ formatDate(selectedStock['上市日期']) }}
        </el-descriptions-item>
        <el-descriptions-item label="所属行业">
          {{ selectedStock['所属行业'] || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="总股本">
          {{ formatNumber(selectedStock['总股本']) }} 亿股
        </el-descriptions-item>
        <el-descriptions-item label="流通股本">
          {{ formatNumber(selectedStock['流通股本']) }} 亿股
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="viewHistory(selectedStock)">查看K线</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

/**
 * 股票名录增强页面组件
 * 支持按市场（沪市、深市、北交所）和板块（主板、科创板、创业板）筛选
 */
export default {
  name: 'StockListEnhanced',
  data() {
    return {
      // 筛选表单数据
      filterForm: {
        market: '',
        board: '',
        searchText: ''
      },
      // 板块选项
      boardOptions: [],
      // 市场板块映射
      marketBoardMap: {
        sh: [
          { label: '主板', value: '主板' },
          { label: '科创板', value: '科创板' }
        ],
        sz: [
          { label: '主板', value: '主板' },
          { label: '创业板', value: '创业板' }
        ],
        bj: [
          { label: '北交所', value: '北交所' }
        ]
      },
      // 股票数据
      shData: [],
      szData: [],
      bjData: [],
      loading: false,
      // 分页配置
      pagination: {
        currentPage: 1,
        pageSize: 50
      },
      // 排序配置
      sortConfig: {
        prop: '',
        order: ''
      },
      // 详情弹窗
      detailDialogVisible: false,
      selectedStock: null
    }
  },
  computed: {
    /**
     * 合并后的所有股票数据
     * @returns {Array} 所有股票数据数组
     */
    allStockData() {
      const data = []
      
      // 处理沪市数据
      this.shData.forEach(item => {
        data.push({
          ...item,
          '市场': 'sh',
          '市场名称': '沪市',
          '板块': this.inferBoard(item['代码'], 'sh')
        })
      })
      
      // 处理深市数据
      this.szData.forEach(item => {
        data.push({
          ...item,
          '市场': 'sz',
          '市场名称': '深市',
          '板块': this.inferBoard(item['代码'], 'sz')
        })
      })
      
      // 处理北交所数据
      this.bjData.forEach(item => {
        data.push({
          ...item,
          '市场': 'bj',
          '市场名称': '北交所',
          '板块': '北交所'
        })
      })
      
      return data
    },
    
    /**
     * 根据筛选条件过滤后的数据
     * @returns {Array} 过滤后的数据数组
     */
    filteredData() {
      let data = [...this.allStockData]
      
      // 按市场筛选
      if (this.filterForm.market) {
        data = data.filter(item => item['市场'] === this.filterForm.market)
      }
      
      // 按板块筛选
      if (this.filterForm.board) {
        data = data.filter(item => item['板块'] === this.filterForm.board)
      }
      
      // 按搜索文本筛选
      if (this.filterForm.searchText) {
        const search = this.filterForm.searchText.toLowerCase()
        data = data.filter(item =>
          (item['代码']?.toLowerCase().includes(search)) ||
          (item['名称']?.toLowerCase().includes(search))
        )
      }
      
      // 排序处理
      if (this.sortConfig.prop && this.sortConfig.order) {
        data.sort((a, b) => {
          let valA = a[this.sortConfig.prop]
          let valB = b[this.sortConfig.prop]
          
          // 数值类型比较
          if (typeof valA === 'string' && !isNaN(parseFloat(valA))) {
            valA = parseFloat(valA)
            valB = parseFloat(valB)
          }
          
          if (valA < valB) {
            return this.sortConfig.order === 'ascending' ? -1 : 1
          }
          if (valA > valB) {
            return this.sortConfig.order === 'ascending' ? 1 : -1
          }
          return 0
        })
      }
      
      return data
    },
    
    /**
     * 分页后的数据
     * @returns {Array} 当前页的数据数组
     */
    paginatedData() {
      const start = (this.pagination.currentPage - 1) * this.pagination.pageSize
      const end = start + this.pagination.pageSize
      return this.filteredData.slice(start, end)
    }
  },
  mounted() {
    // 页面加载时获取所有市场数据
    this.loadAllData()
  },
  methods: {
    /**
     * 加载所有市场数据
     */
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadShData(),
          this.loadSzData(),
          this.loadBjData()
        ])
        this.$message.success('数据加载完成')
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 加载沪市股票数据
     */
    async loadShData() {
      try {
        const response = await stockApi.getShNameCode()
        this.shData = response.data || []
      } catch (error) {
        console.error('加载沪市数据失败:', error)
        this.shData = []
      }
    },
    
    /**
     * 加载深市股票数据
     */
    async loadSzData() {
      try {
        const response = await stockApi.getSzNameCode()
        this.szData = response.data || []
      } catch (error) {
        console.error('加载深市数据失败:', error)
        this.szData = []
      }
    },
    
    /**
     * 加载北交所股票数据
     */
    async loadBjData() {
      try {
        const response = await stockApi.getBjNameCode()
        this.bjData = response.data || []
      } catch (error) {
        console.error('加载北交所数据失败:', error)
        this.bjData = []
      }
    },
    
    /**
     * 处理市场选择变化
     * @param {string} value - 选中的市场值
     */
    handleMarketChange(value) {
      // 清空板块选择
      this.filterForm.board = ''
      // 更新板块选项
      this.boardOptions = value ? this.marketBoardMap[value] || [] : []
      // 重置分页
      this.pagination.currentPage = 1
    },
    
    /**
     * 处理查询按钮点击
     */
    handleSearch() {
      this.pagination.currentPage = 1
    },
    
    /**
     * 处理重置按钮点击
     */
    handleReset() {
      this.filterForm = {
        market: '',
        board: '',
        searchText: ''
      }
      this.boardOptions = []
      this.pagination.currentPage = 1
    },
    
    /**
     * 处理分页大小变化
     * @param {number} size - 新的分页大小
     */
    handleSizeChange(size) {
      this.pagination.pageSize = size
      this.pagination.currentPage = 1
    },
    
    /**
     * 处理页码变化
     * @param {number} page - 新的页码
     */
    handleCurrentChange(page) {
      this.pagination.currentPage = page
    },
    
    /**
     * 处理排序变化
     * @param {Object} params - 排序参数
     */
    handleSortChange(params) {
      this.sortConfig.prop = params.prop
      this.sortConfig.order = params.order
    },
    
    /**
     * 根据股票代码推断所属板块
     * @param {string} code - 股票代码
     * @param {string} market - 市场代码
     * @returns {string} 板块名称
     */
    inferBoard(code, market) {
      if (!code) return '-'
      
      if (market === 'sh') {
        // 沪市：688开头为科创板，其余为主板
        return code.startsWith('688') ? '科创板' : '主板'
      } else if (market === 'sz') {
        // 深市：300开头为创业板，其余为主板
        return code.startsWith('300') || code.startsWith('301') ? '创业板' : '主板'
      }
      
      return '-'
    },
    
    /**
     * 获取市场标签类型
     * @param {string} market - 市场代码
     * @returns {string} Element UI标签类型
     */
    getMarketTagType(market) {
      const typeMap = {
        'sh': 'danger',
        'sz': 'success',
        'bj': 'warning'
      }
      return typeMap[market] || 'info'
    },
    
    /**
     * 获取市场显示标签
     * @param {string} market - 市场代码
     * @returns {string} 市场显示名称
     */
    getMarketLabel(market) {
      const labelMap = {
        'sh': '沪市',
        'sz': '深市',
        'bj': '北交所'
      }
      return labelMap[market] || market
    },
    
    /**
     * 获取板块标签类型
     * @param {string} board - 板块名称
     * @returns {string} Element UI标签类型
     */
    getBoardTagType(board) {
      const typeMap = {
        '主板': 'primary',
        '科创板': 'danger',
        '创业板': 'success',
        '北交所': 'warning'
      }
      return typeMap[board] || 'info'
    },
    
    /**
     * 获取板块显示标签
     * @param {string} board - 板块代码
     * @returns {string} 板块显示名称
     */
    getBoardLabel(board) {
      return board || '全部板块'
    },
    
    /**
     * 格式化日期显示
     * @param {string} dateStr - 日期字符串
     * @returns {string} 格式化后的日期
     */
    formatDate(dateStr) {
      if (!dateStr) return '-'
      // 处理YYYYMMDD格式
      if (dateStr.length === 8) {
        return `${dateStr.substring(0, 4)}-${dateStr.substring(4, 6)}-${dateStr.substring(6, 8)}`
      }
      return dateStr
    },
    
    /**
     * 格式化数字显示
     * @param {string|number} value - 需要格式化的数值
     * @returns {string} 格式化后的字符串
     */
    formatNumber(value) {
      if (value === undefined || value === null || value === '-') return '-'
      const num = parseFloat(value)
      if (isNaN(num)) return value
      return num.toLocaleString(undefined, { maximumFractionDigits: 2 })
    },
    
    /**
     * 查看股票历史K线
     * @param {Object} row - 股票数据行
     */
    viewHistory(row) {
      if (!row || !row['代码']) {
        this.$message.warning('股票代码无效')
        return
      }
      this.$router.push({
        path: '/stock/history',
        query: { symbol: row['代码'] }
      })
    },
    
    /**
     * 查看股票详情
     * @param {Object} row - 股票数据行
     */
    viewDetail(row) {
      this.selectedStock = row
      this.detailDialogVisible = true
    },
    
    /**
     * 导出数据
     */
    exportData() {
      const data = this.filteredData
      if (data.length === 0) {
        this.$message.warning('没有可导出的数据')
        return
      }
      
      // 构建CSV内容
      const headers = ['股票代码', '股票名称', '所属市场', '所属板块', '上市日期', '总股本(亿股)', '流通股本(亿股)', '所属行业']
      const rows = data.map(item => [
        item['代码'],
        item['名称'],
        this.getMarketLabel(item['市场']),
        item['板块'] || '-',
        this.formatDate(item['上市日期']),
        item['总股本'] || '-',
        item['流通股本'] || '-',
        item['所属行业'] || '-'
      ])
      
      const csvContent = [headers.join(','), ...rows.map(row => row.join(','))].join('\n')
      
      // 下载文件
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `股票名录_${new Date().toISOString().split('T')[0]}.csv`
      link.click()
      
      this.$message.success('数据导出成功')
    }
  }
}
</script>

<style scoped>
@import '../styles/StockListEnhanced.css';
</style>
