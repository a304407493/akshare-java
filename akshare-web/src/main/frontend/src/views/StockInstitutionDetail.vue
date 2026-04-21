<template>
  <!-- 龙虎榜详情页面容器 -->
  <div class="stock-institution-detail">
    <!-- 主卡片组件 -->
    <el-card>
      <!-- 卡片头部：标题、返回按钮和刷新按钮 -->
      <div slot="header" class="clearfix">
        <span>
          <el-button type="text" @click="goBack" style="padding: 0; margin-right: 10px;">
            <i class="el-icon-arrow-left"></i> 返回
          </el-button>
          {{ stockName }}({{ stockSymbol }}) - 龙虎榜详情
        </span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 查询条件区域 -->
      <div class="search-area">
        <!-- 日期选择器 -->
        <el-date-picker
          v-model="queryDate"
          type="date"
          placeholder="选择日期"
          value-format="yyyyMMdd"
          style="margin-right: 10px;"
        ></el-date-picker>
        <!-- 买卖方向选择器 -->
        <el-select v-model="flag" placeholder="买卖方向" style="margin-right: 10px; width: 120px;">
          <el-option label="全部" value=""></el-option>
          <el-option label="买入" value="买入"></el-option>
          <el-option label="卖出" value="卖出"></el-option>
        </el-select>
        <!-- 查询按钮 -->
        <el-button type="primary" @click="handleSearch">
          <i class="el-icon-search"></i> 查询
        </el-button>
      </div>

      <!-- 股票基本信息卡片 -->
      <div class="stock-info" v-if="stockInfo">
        <el-row :gutter="20">
          <!-- 收盘价 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">收盘价</div>
              <div class="info-value">{{ formatNumber(stockInfo['收盘价']) }}</div>
            </div>
          </el-col>
          <!-- 涨跌幅 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">涨跌幅</div>
              <div class="info-value" :class="getChangeClass(stockInfo['涨跌幅'])">
                {{ formatChange(stockInfo['涨跌幅']) }}
              </div>
            </div>
          </el-col>
          <!-- 龙虎榜净买额 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">净买额(万)</div>
              <div class="info-value" :class="getAmountClass(stockInfo['龙虎榜净买额'])">
                {{ formatAmount(stockInfo['龙虎榜净买额']) }}
              </div>
            </div>
          </el-col>
          <!-- 龙虎榜买入额 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">买入额(万)</div>
              <div class="info-value positive-amount">{{ formatAmount(stockInfo['龙虎榜买入额']) }}</div>
            </div>
          </el-col>
          <!-- 龙虎榜卖出额 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">卖出额(万)</div>
              <div class="info-value negative-amount">{{ formatAmount(stockInfo['龙虎榜卖出额']) }}</div>
            </div>
          </el-col>
          <!-- 换手率 -->
          <el-col :span="4">
            <div class="info-item">
              <div class="info-label">换手率</div>
              <div class="info-value">{{ formatPercent(stockInfo['换手率']) }}</div>
            </div>
          </el-col>
        </el-row>
        <!-- 上榜原因和解读 -->
        <el-row :gutter="20" style="margin-top: 15px;">
          <el-col :span="12">
            <div class="info-item">
              <div class="info-label">上榜原因</div>
              <div class="info-text">{{ stockInfo['上榜原因'] || '-' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="info-item">
              <div class="info-label">解读</div>
              <div class="info-text">{{ stockInfo['解读'] || '-' }}</div>
            </div>
          </el-col>
        </el-row>
      </div>

      <!-- ECharts图表区域：买卖金额对比 -->
      <div class="chart-area" v-if="chartData.length > 0">
        <div ref="tradeChart" style="width: 100%; height: 400px;"></div>
      </div>

      <!-- 买卖席位表格区域 -->
      <el-divider content-position="left">龙虎榜数据</el-divider>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        stripe
        style="width: 100%;"
        height="400"
        v-loading="loading"
      >
        <!-- 序号列 -->
        <el-table-column prop="序号" label="序号" width="60"></el-table-column>
        <!-- 日期列 -->
        <el-table-column prop="上榜日" label="上榜日期" width="120"></el-table-column>
        <!-- 解读列 -->
        <el-table-column prop="解读" label="解读" min-width="200" show-overflow-tooltip></el-table-column>
        <!-- 收盘价列 -->
        <el-table-column prop="收盘价" label="收盘价" width="100">
          <template slot-scope="scope">
            <span>{{ formatNumber(scope.row['收盘价']) }}</span>
          </template>
        </el-table-column>
        <!-- 涨跌幅列 -->
        <el-table-column prop="涨跌幅" label="涨跌幅" width="100">
          <template slot-scope="scope">
            <span :class="getChangeClass(scope.row['涨跌幅'])">
              {{ formatChange(scope.row['涨跌幅']) }}
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
        <!-- 龙虎榜净买额列 -->
        <el-table-column prop="龙虎榜净买额" label="净买额(万)" width="120">
          <template slot-scope="scope">
            <span :class="getAmountClass(scope.row['龙虎榜净买额'])">
              {{ formatAmount(scope.row['龙虎榜净买额']) }}
            </span>
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
      </el-table>
    </el-card>
  </div>
</template>

<script>
// 导入机构数据API
import { institutionApi } from '../utils/api'
// 导入ECharts图表库
import * as echarts from 'echarts'

export default {
  name: 'StockInstitutionDetail',
  data() {
    return {
      // 股票代码
      stockSymbol: '',
      // 股票名称
      stockName: '',
      // 查询日期
      queryDate: '',
      // 买卖方向筛选
      flag: '',
      // 表格数据
      tableData: [],
      // 图表数据
      chartData: [],
      // 股票基本信息
      stockInfo: null,
      // 加载状态
      loading: false,
      // ECharts实例
      chartInstance: null
    }
  },
  mounted() {
    // 从路由参数获取股票信息
    this.stockSymbol = this.$route.query.symbol || ''
    this.stockName = this.$route.query.name || ''
    this.queryDate = this.$route.query.date || this.getDefaultDate()
    // 加载数据
    this.loadData()
    // 监听窗口大小变化，自适应调整图表
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前移除事件监听并销毁图表实例
    window.removeEventListener('resize', this.handleResize)
    if (this.chartInstance) {
      this.chartInstance.dispose()
    }
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
     * 加载龙虎榜详情数据
     * 调用后端API获取指定股票的龙虎榜详情
     */
    async loadData() {
      if (!this.stockSymbol) {
        this.$message.error('股票代码不能为空')
        return
      }
      this.loading = true
      try {
        const response = await institutionApi.getLhbStockDetail(
          this.stockSymbol,
          this.queryDate,
          this.flag
        )
        const data = response.data || []
        this.tableData = data
        // 提取第一条数据作为股票基本信息
        if (data.length > 0) {
          this.stockInfo = data[0]
        }
        // 处理图表数据
        this.processChartData(data)
        // 渲染图表
        this.$nextTick(() => {
          this.renderChart()
        })
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    /**
     * 处理图表数据
     * 使用龙虎榜汇总数据展示买卖金额对比
     * @param {Array} data 原始数据
     */
    processChartData(data) {
      if (!data || data.length === 0) {
        this.chartData = []
        return
      }
      // 使用第一条数据的买卖金额
      const item = data[0]
      const buyAmt = Number(item['龙虎榜买入额']) || 0
      const sellAmt = Number(item['龙虎榜卖出额']) || 0
      
      // 构建图表数据
      this.chartData = [{
        name: '龙虎榜',
        buy: buyAmt,
        sell: sellAmt
      }]
    },
    /**
     * 渲染ECharts柱状图
     * 展示买卖金额对比
     */
    renderChart() {
      if (!this.$refs.tradeChart || this.chartData.length === 0) return
      // 如果已有实例则销毁
      if (this.chartInstance) {
        this.chartInstance.dispose()
      }
      // 初始化图表
      this.chartInstance = echarts.init(this.$refs.tradeChart)
      // 配置选项
      const option = {
        title: {
          text: '买卖席位金额对比（万元）',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(item => {
              result += item.marker + item.seriesName + ': ' + item.value.toFixed(2) + '万<br/>'
            })
            return result
          }
        },
        legend: {
          data: ['买入金额', '卖出金额'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '15%',
          top: '20%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.chartData.map(item => item.name),
          axisLabel: {
            rotate: 45,
            interval: 0,
            fontSize: 10
          }
        },
        yAxis: {
          type: 'value',
          name: '金额（万元）',
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [
          {
            name: '买入金额',
            type: 'bar',
            data: this.chartData.map(item => item.buy / 10000),
            itemStyle: {
              color: '#f56c6c'
            }
          },
          {
            name: '卖出金额',
            type: 'bar',
            data: this.chartData.map(item => item.sell / 10000),
            itemStyle: {
              color: '#67c23a'
            }
          }
        ]
      }
      // 设置图表选项
      this.chartInstance.setOption(option)
    },
    /**
     * 处理窗口大小变化
     * 自适应调整图表大小
     */
    handleResize() {
      if (this.chartInstance) {
        this.chartInstance.resize()
      }
    },
    /**
     * 处理查询按钮点击
     * 重新加载数据
     */
    handleSearch() {
      this.loadData()
    },
    /**
     * 返回上一页
     */
    goBack() {
      this.$router.back()
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
.stock-institution-detail {
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

/* 股票信息区域样式 */
.stock-info {
  background-color: #f5f7fa;
  padding: 20px;
  border-radius: 4px;
  margin-bottom: 20px;
}

/* 信息项样式 */
.info-item {
  text-align: center;
}

/* 信息标签样式 */
.info-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

/* 信息值样式 */
.info-value {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

/* 信息文本样式 */
.info-text {
  font-size: 14px;
  color: #606266;
  text-align: left;
  line-height: 1.5;
}

/* 图表区域样式 */
.chart-area {
  margin: 20px 0;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 上涨样式（红色） */
.positive-change {
  color: #f56c6c;
}

/* 下跌样式（绿色） */
.negative-change {
  color: #67c23a;
}

/* 正金额样式（红色） */
.positive-amount {
  color: #f56c6c;
  font-weight: bold;
}

/* 负金额样式（绿色） */
.negative-amount {
  color: #67c23a;
  font-weight: bold;
}
</style>
