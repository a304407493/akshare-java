<template>
  <div class="stock-delist">
    <!-- 页面标题区域 -->
    <div class="page-header">
      <h2>退市股票</h2>
      <p class="page-desc">展示上交所、深交所已退市股票列表及退市信息</p>
    </div>

    <!-- 统计卡片区域 -->
    <el-row :gutter="20" class="summary-cards">
      <!-- 沪市退市统计 -->
      <el-col :span="8">
        <el-card class="stat-card sh-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-office-building"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ shDelistData.length }}</div>
            <div class="stat-label">沪市退市股票</div>
          </div>
        </el-card>
      </el-col>

      <!-- 深市退市统计 -->
      <el-col :span="8">
        <el-card class="stat-card sz-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-office-building"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ szDelistData.length }}</div>
            <div class="stat-label">深市退市股票</div>
          </div>
        </el-card>
      </el-col>

      <!-- 总计统计 -->
      <el-col :span="8">
        <el-card class="stat-card total-card" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-s-data"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ shDelistData.length + szDelistData.length }}</div>
            <div class="stat-label">退市股票总数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选条件区域 -->
    <el-card class="filter-card">
      <div slot="header" class="card-header">
        <span>筛选条件</span>
        <el-button type="primary" size="mini" @click="loadAllData">
          <i class="el-icon-refresh"></i> 刷新数据
        </el-button>
      </div>
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <!-- 市场筛选 -->
        <el-form-item label="所属市场">
          <el-select
            v-model="filterForm.market"
            placeholder="请选择市场"
            clearable
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
          共 {{ filteredData.length }} 只退市股票
        </el-tag>
        <el-tag v-if="filterForm.market" type="success" effect="plain" style="margin-left: 10px">
          {{ getMarketLabel(filterForm.market) }}
        </el-tag>
      </div>
    </el-card>

    <!-- 退市股票列表表格 -->
    <el-card class="table-card">
      <div slot="header" class="card-header">
        <span>退市股票列表</span>
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

        <!-- 退市日期 -->
        <el-table-column
          prop="退市日期"
          label="退市日期"
          min-width="120"
          sortable
        >
          <template slot-scope="scope">
            <el-tag type="danger" size="small" effect="plain">
              <i class="el-icon-warning"></i>
              {{ formatDate(scope.row['退市日期']) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 上市时长 -->
        <el-table-column
          label="上市时长"
          min-width="120"
          sortable
          :sort-method="sortByDuration"
        >
          <template slot-scope="scope">
            <span class="duration-cell">
              {{ calculateDuration(scope.row['上市日期'], scope.row['退市日期']) }}
            </span>
          </template>
        </el-table-column>

        <!-- 退市原因 -->
        <el-table-column
          prop="退市原因"
          label="退市原因"
          min-width="200"
          show-overflow-tooltip
        >
          <template slot-scope="scope">
            <span v-if="scope.row['退市原因']">
              {{ scope.row['退市原因'] }}
            </span>
            <el-tag v-else type="info" size="small" effect="plain">未披露</el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column
          label="操作"
          min-width="120"
          fixed="right"
          align="center"
        >
          <template slot-scope="scope">
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
      title="退市股票详情"
      :visible.sync="detailDialogVisible"
      width="600px"
    >
      <el-alert
        title="该股票已退市"
        type="error"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      ></el-alert>
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
        <el-descriptions-item label="上市时长">
          {{ calculateDuration(selectedStock['上市日期'], selectedStock['退市日期']) }}
        </el-descriptions-item>
        <el-descriptions-item label="上市日期">
          {{ formatDate(selectedStock['上市日期']) }}
        </el-descriptions-item>
        <el-descriptions-item label="退市日期">
          <el-tag type="danger" size="small">
            {{ formatDate(selectedStock['退市日期']) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="退市原因" :span="2">
          {{ selectedStock['退市原因'] || '未披露' }}
        </el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

/**
 * 退市股票页面组件
 * 展示上交所、深交所已退市股票列表及退市信息
 */
export default {
  name: 'StockDelist',
  data() {
    return {
      // 筛选表单数据
      filterForm: {
        market: '',
        searchText: ''
      },
      // 退市股票数据
      shDelistData: [],
      szDelistData: [],
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
     * 合并后的所有退市股票数据
     * @returns {Array} 所有退市股票数据数组
     */
    allDelistData() {
      const data = []
      
      // 处理沪市退市数据
      this.shDelistData.forEach(item => {
        data.push({
          ...item,
          '市场': 'sh',
          '市场名称': '沪市'
        })
      })
      
      // 处理深市退市数据
      this.szDelistData.forEach(item => {
        data.push({
          ...item,
          '市场': 'sz',
          '市场名称': '深市'
        })
      })
      
      return data
    },
    
    /**
     * 根据筛选条件过滤后的数据
     * @returns {Array} 过滤后的数据数组
     */
    filteredData() {
      let data = [...this.allDelistData]
      
      // 按市场筛选
      if (this.filterForm.market) {
        data = data.filter(item => item['市场'] === this.filterForm.market)
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
    // 页面加载时获取退市数据
    this.loadAllData()
  },
  methods: {
    /**
     * 加载所有退市数据
     */
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadShDelistData(),
          this.loadSzDelistData()
        ])
        this.$message.success('退市数据加载完成')
      } catch (error) {
        this.$message.error('加载退市数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    /**
     * 加载沪市退市股票数据
     */
    async loadShDelistData() {
      try {
        const response = await stockApi.getShDelist()
        this.shDelistData = response.data || []
      } catch (error) {
        console.error('加载沪市退市数据失败:', error)
        this.shDelistData = []
      }
    },
    
    /**
     * 加载深市退市股票数据
     */
    async loadSzDelistData() {
      try {
        const response = await stockApi.getSzDelist()
        this.szDelistData = response.data || []
      } catch (error) {
        console.error('加载深市退市数据失败:', error)
        this.szDelistData = []
      }
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
        searchText: ''
      }
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
     * 按上市时长排序
     * @param {Object} a - 第一条数据
     * @param {Object} b - 第二条数据
     * @returns {number} 比较结果
     */
    sortByDuration(a, b) {
      const durationA = this.calculateDurationDays(a['上市日期'], a['退市日期'])
      const durationB = this.calculateDurationDays(b['上市日期'], b['退市日期'])
      return durationA - durationB
    },
    
    /**
     * 获取市场标签类型
     * @param {string} market - 市场代码
     * @returns {string} Element UI标签类型
     */
    getMarketTagType(market) {
      const typeMap = {
        'sh': 'danger',
        'sz': 'success'
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
        'sz': '深市'
      }
      return labelMap[market] || market
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
      // 处理YYYY-MM-DD格式
      return dateStr
    },
    
    /**
     * 计算上市时长
     * @param {string} listDate - 上市日期
     * @param {string} delistDate - 退市日期
     * @returns {string} 格式化后的时长
     */
    calculateDuration(listDate, delistDate) {
      const days = this.calculateDurationDays(listDate, delistDate)
      if (days < 0) return '-'
      
      const years = Math.floor(days / 365)
      const months = Math.floor((days % 365) / 30)
      
      if (years > 0) {
        return `${years}年${months}个月`
      } else if (months > 0) {
        return `${months}个月`
      } else {
        return `${days}天`
      }
    },
    
    /**
     * 计算上市时长天数
     * @param {string} listDate - 上市日期
     * @param {string} delistDate - 退市日期
     * @returns {number} 天数
     */
    calculateDurationDays(listDate, delistDate) {
      if (!listDate || !delistDate) return -1
      
      const start = this.parseDate(listDate)
      const end = this.parseDate(delistDate)
      
      if (!start || !end) return -1
      
      const diffTime = end.getTime() - start.getTime()
      return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    },
    
    /**
     * 解析日期字符串
     * @param {string} dateStr - 日期字符串
     * @returns {Date|null} Date对象
     */
    parseDate(dateStr) {
      if (!dateStr) return null
      
      // 处理YYYYMMDD格式
      if (dateStr.length === 8 && !dateStr.includes('-')) {
        const year = parseInt(dateStr.substring(0, 4))
        const month = parseInt(dateStr.substring(4, 6)) - 1
        const day = parseInt(dateStr.substring(6, 8))
        return new Date(year, month, day)
      }
      
      // 处理YYYY-MM-DD格式
      return new Date(dateStr)
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
      const headers = ['股票代码', '股票名称', '所属市场', '上市日期', '退市日期', '上市时长', '退市原因']
      const rows = data.map(item => [
        item['代码'],
        item['名称'],
        this.getMarketLabel(item['市场']),
        this.formatDate(item['上市日期']),
        this.formatDate(item['退市日期']),
        this.calculateDuration(item['上市日期'], item['退市日期']),
        item['退市原因'] || '未披露'
      ])
      
      const csvContent = [headers.join(','), ...rows.map(row => row.join(','))].join('\n')
      
      // 下载文件
      const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `退市股票_${new Date().toISOString().split('T')[0]}.csv`
      link.click()
      
      this.$message.success('数据导出成功')
    }
  }
}
</script>

<style scoped>
@import '../styles/StockDelist.css';
</style>
