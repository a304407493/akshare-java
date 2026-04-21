<template>
  <!-- 龙虎榜主页面容器 -->
  <div class="stock-institution">
    <!-- 主卡片组件 -->
    <el-card>
      <!-- 卡片头部：标题和刷新按钮 -->
      <div slot="header" class="clearfix">
        <span>龙虎榜数据</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 查询条件区域 -->
      <div class="search-area">
        <!-- 开始日期选择器 -->
        <el-date-picker
          v-model="startDate"
          type="date"
          placeholder="开始日期"
          value-format="yyyyMMdd"
          style="margin-right: 10px;"
        ></el-date-picker>
        <!-- 结束日期选择器 -->
        <el-date-picker
          v-model="endDate"
          type="date"
          placeholder="结束日期"
          value-format="yyyyMMdd"
          style="margin-right: 10px;"
        ></el-date-picker>
        <!-- 查询按钮 -->
        <el-button type="primary" @click="handleSearch">
          <i class="el-icon-search"></i> 查询
        </el-button>
      </div>

      <!-- 数据表格区域 -->
      <el-table
        :data="tableData"
        stripe
        style="width: 100%;"
        height="600"
        v-loading="loading"
      >
        <!-- 序号列 -->
        <el-table-column prop="序号" label="序号" width="60"></el-table-column>
        <!-- 股票代码列 -->
        <el-table-column prop="代码" label="代码" width="100"></el-table-column>
        <!-- 股票名称列 -->
        <el-table-column prop="名称" label="名称" width="120"></el-table-column>
        <!-- 上榜日期列 -->
        <el-table-column prop="上榜日" label="上榜日期" width="120"></el-table-column>
        <!-- 收盘价列 -->
        <el-table-column prop="收盘价" label="收盘价" width="100">
          <template slot-scope="scope">
            <span>{{ formatNumber(scope.row['收盘价']) }}</span>
          </template>
        </el-table-column>
        <!-- 涨跌幅列：根据正负值显示不同颜色 -->
        <el-table-column prop="涨跌幅" label="涨跌幅" width="100">
          <template slot-scope="scope">
            <span :class="getChangeClass(scope.row['涨跌幅'])">
              {{ formatChange(scope.row['涨跌幅']) }}
            </span>
          </template>
        </el-table-column>
        <!-- 龙虎榜净买额列 -->
        <el-table-column prop="龙虎榜净买额" label="净买额(万)" width="120">
          <template slot-scope="scope">
            <span :class="getAmountClass(scope.row['龙虎榜净买额'])">
              {{ formatAmount(scope.row['龙虎榜净买额']) }}
            </span>
          </template>
        </el-table-column>
        <!-- 龙虎榜买入额列 -->
        <el-table-column prop="龙虎榜买入额" label="买入额(万)" width="120">
          <template slot-scope="scope">
            <span class="positive-amount">{{ formatAmount(scope.row['龙虎榜买入额']) }}</span>
          </template>
        </el-table-column>
        <!-- 龙虎榜卖出额列 -->
        <el-table-column prop="龙虎榜卖出额" label="卖出额(万)" width="120">
          <template slot-scope="scope">
            <span class="negative-amount">{{ formatAmount(scope.row['龙虎榜卖出额']) }}</span>
          </template>
        </el-table-column>
        <!-- 龙虎榜成交额列 -->
        <el-table-column prop="龙虎榜成交额" label="成交额(万)" width="120">
          <template slot-scope="scope">
            <span>{{ formatAmount(scope.row['龙虎榜成交额']) }}</span>
          </template>
        </el-table-column>
        <!-- 换手率列 -->
        <el-table-column prop="换手率" label="换手率" width="100">
          <template slot-scope="scope">
            <span>{{ formatPercent(scope.row['换手率']) }}</span>
          </template>
        </el-table-column>
        <!-- 上榜原因列 -->
        <el-table-column prop="上榜原因" label="上榜原因" min-width="200" show-overflow-tooltip></el-table-column>
        <!-- 解读列 -->
        <el-table-column prop="解读" label="解读" min-width="150" show-overflow-tooltip></el-table-column>
        <!-- 操作列：查看详情按钮 -->
        <el-table-column label="操作" width="120" fixed="right">
          <template slot-scope="scope">
            <el-button
              size="mini"
              type="primary"
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <div class="pagination-area">
        <el-pagination
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :current-page="currentPage"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="pageSize"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
// 导入机构数据API
import { institutionApi } from '../utils/api'

export default {
  name: 'StockInstitution',
  data() {
    return {
      // 开始日期，默认为今天
      startDate: this.getDefaultDate(),
      // 结束日期，默认为今天
      endDate: this.getDefaultDate(),
      // 表格数据
      tableData: [],
      // 加载状态
      loading: false,
      // 当前页码
      currentPage: 1,
      // 每页条数
      pageSize: 20,
      // 总记录数
      total: 0
    }
  },
  computed: {
    /**
     * 分页后的数据
     * @returns {Array} 当前页的数据
     */
    paginatedData() {
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.tableData.slice(start, end)
    }
  },
  mounted() {
    // 组件挂载后加载数据
    this.loadData()
  },
  methods: {
    /**
     * 获取默认日期（今天）
     * @returns {string} 格式化的日期字符串 yyyyMMdd
     */
    getDefaultDate() {
      const date = new Date()
      const year = date.getFullYear()
      const month = String(date.getMonth() + 1).padStart(2, '0')
      const day = String(date.getDate()).padStart(2, '0')
      return `${year}${month}${day}`
    },
    /**
     * 加载龙虎榜数据
     * 调用后端API获取指定日期范围内的龙虎榜详情
     */
    async loadData() {
      this.loading = true
      try {
        const response = await institutionApi.getLhbDetail(this.startDate, this.endDate)
        this.tableData = response.data || []
        this.total = this.tableData.length
        this.$message.success(`成功加载 ${this.total} 条数据`)
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    /**
     * 处理查询按钮点击
     * 重置页码并重新加载数据
     */
    handleSearch() {
      this.currentPage = 1
      this.loadData()
    },
    /**
     * 查看个股详情
     * 跳转到龙虎榜详情页面
     * @param {Object} row 当前行数据
     */
    viewDetail(row) {
      this.$router.push({
        path: '/stock/institution/detail',
        query: {
          symbol: row['代码'],
          name: row['名称'],
          date: row['上榜日']
        }
      })
    },
    /**
     * 处理每页条数变化
     * @param {number} val 新的每页条数
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    /**
     * 处理页码变化
     * @param {number} val 新的页码
     */
    handleCurrentChange(val) {
      this.currentPage = val
    },
    /**
     * 格式化数字
     * @param {number} value 数值
     * @returns {string} 格式化后的字符串
     */
    formatNumber(value) {
      if (value === null || value === undefined) return '-'
      return Number(value).toFixed(2)
    },
    /**
     * 格式化涨跌幅，添加%符号
     * @param {number} value 涨跌幅数值
     * @returns {string} 格式化后的字符串
     */
    formatChange(value) {
      if (value === null || value === undefined) return '-'
      const num = Number(value)
      return (num > 0 ? '+' : '') + num.toFixed(2) + '%'
    },
    /**
     * 格式化金额，转换为万为单位
     * @param {number} value 金额数值
     * @returns {string} 格式化后的字符串
     */
    formatAmount(value) {
      if (value === null || value === undefined) return '-'
      return (Number(value) / 10000).toFixed(2)
    },
    /**
     * 格式化百分比
     * @param {number} value 百分比数值
     * @returns {string} 格式化后的字符串
     */
    formatPercent(value) {
      if (value === null || value === undefined) return '-'
      return Number(value).toFixed(2) + '%'
    },
    /**
     * 获取涨跌幅的样式类名
     * @param {number} value 涨跌幅数值
     * @returns {string} 样式类名
     */
    getChangeClass(value) {
      if (value === null || value === undefined) return ''
      const num = Number(value)
      if (num > 0) return 'positive-change'
      if (num < 0) return 'negative-change'
      return ''
    },
    /**
     * 获取金额的正负样式类名
     * @param {number} value 金额数值
     * @returns {string} 样式类名
     */
    getAmountClass(value) {
      if (value === null || value === undefined) return ''
      const num = Number(value)
      if (num > 0) return 'positive-amount'
      if (num < 0) return 'negative-amount'
      return ''
    }
  }
}
</script>

<style scoped>
/* 页面容器样式 */
.stock-institution {
  padding: 0;
}

/* 头部清除浮动样式 */
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

/* 查询区域样式 */
.search-area {
  margin-bottom: 20px;
}

/* 分页区域样式 */
.pagination-area {
  margin-top: 20px;
  text-align: right;
}

/* 上涨样式（红色） */
.positive-change {
  color: #f56c6c;
  font-weight: bold;
}

/* 下跌样式（绿色） */
.negative-change {
  color: #67c23a;
  font-weight: bold;
}

/* 正金额样式（红色） */
.positive-amount {
  color: #f56c6c;
}

/* 负金额样式（绿色） */
.negative-amount {
  color: #67c23a;
}
</style>
