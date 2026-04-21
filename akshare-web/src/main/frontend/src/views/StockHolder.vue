<template>
  <div class="stock-holder">
    <el-card>
      <div slot="header" class="clearfix">
        <span>股东数据</span>
      </div>

      <!-- 查询条件区域 -->
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="股票代码">
          <el-input v-model="queryForm.symbol" placeholder="请输入股票代码" clearable></el-input>
        </el-form-item>
        <el-form-item label="报告期">
          <el-input v-model="queryForm.date" placeholder="格式：20240930" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 饼图展示区域 -->
      <el-row :gutter="20" class="chart-row">
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>十大流通股东持股比例分布</span>
            </div>
            <div ref="freeHolderPieChart" class="pie-chart"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <div slot="header">
              <span>十大股东持股比例分布</span>
            </div>
            <div ref="holderPieChart" class="pie-chart"></div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 十大流通股东列表 -->
      <el-card class="table-card">
        <div slot="header">
          <span>十大流通股东</span>
        </div>
        <el-table :data="freeHolderList" stripe style="width: 100%" v-loading="loadingFree">
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="股东名称" label="股东名称" min-width="200"></el-table-column>
          <el-table-column prop="持股数量" label="持股数量" width="150">
            <template slot-scope="scope">
              {{ formatNumber(scope.row['持股数量']) }}
            </template>
          </el-table-column>
          <el-table-column prop="持股比例" label="持股比例" width="120">
            <template slot-scope="scope">
              <el-progress :percentage="parseFloat(scope.row['持股比例']) || 0" :color="progressColors"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="持股变动" label="持股变动" width="120">
            <template slot-scope="scope">
              <span :class="getChangeClass(scope.row['持股变动'])">
                {{ formatChange(scope.row['持股变动']) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="变动比例" label="变动比例" width="120">
            <template slot-scope="scope">
              <span :class="getChangeClass(scope.row['变动比例'])">
                {{ formatPercent(scope.row['变动比例']) }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <!-- 十大股东列表 -->
      <el-card class="table-card">
        <div slot="header">
          <span>十大股东</span>
        </div>
        <el-table :data="holderList" stripe style="width: 100%" v-loading="loadingHolder">
          <el-table-column type="index" label="序号" width="80"></el-table-column>
          <el-table-column prop="股东名称" label="股东名称" min-width="200"></el-table-column>
          <el-table-column prop="持股数量" label="持股数量" width="150">
            <template slot-scope="scope">
              {{ formatNumber(scope.row['持股数量']) }}
            </template>
          </el-table-column>
          <el-table-column prop="持股比例" label="持股比例" width="120">
            <template slot-scope="scope">
              <el-progress :percentage="parseFloat(scope.row['持股比例']) || 0" :color="progressColors"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="股份类型" label="股份类型" width="120"></el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { stockApi } from '../utils/api'

export default {
  name: 'StockHolder',
  data() {
    return {
      // 查询表单数据
      queryForm: {
        symbol: '000001',
        date: '20240930'  // 默认报告期日期
      },
      // 十大流通股东列表数据
      freeHolderList: [],
      // 十大股东列表数据
      holderList: [],
      // 加载状态
      loadingFree: false,
      loadingHolder: false,
      // 饼图实例
      freeHolderPieChart: null,
      holderPieChart: null,
      // 进度条颜色配置
      progressColors: [
        { color: '#f56c6c', percentage: 20 },
        { color: '#e6a23c', percentage: 40 },
        { color: '#5cb87a', percentage: 60 },
        { color: '#1989fa', percentage: 80 },
        { color: '#6f7ad3', percentage: 100 }
      ]
    }
  },
  mounted() {
    // 初始化图表
    this.initCharts()
    // 加载数据
    this.loadData()
    // 监听窗口大小变化
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 销毁图表实例
    if (this.freeHolderPieChart) {
      this.freeHolderPieChart.dispose()
    }
    if (this.holderPieChart) {
      this.holderPieChart.dispose()
    }
    // 移除事件监听
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    /**
     * 初始化ECharts图表实例
     */
    initCharts() {
      this.freeHolderPieChart = echarts.init(this.$refs.freeHolderPieChart)
      this.holderPieChart = echarts.init(this.$refs.holderPieChart)
    },

    /**
     * 处理窗口大小变化，调整图表尺寸
     */
    handleResize() {
      if (this.freeHolderPieChart) {
        this.freeHolderPieChart.resize()
      }
      if (this.holderPieChart) {
        this.holderPieChart.resize()
      }
    },

    /**
     * 加载股东数据
     */
    async loadData() {
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }
      // 并行加载流通股东和股东数据
      await Promise.all([
        this.loadFreeHolderData(),
        this.loadHolderData()
      ])
    },

    /**
     * 加载十大流通股东数据
     */
    async loadFreeHolderData() {
      this.loadingFree = true
      try {
        const response = await stockApi.getTop10FreeHolders(this.queryForm.symbol, this.queryForm.date)
        this.freeHolderList = response.data || []
        // 渲染流通股东饼图
        this.renderFreeHolderPieChart()
      } catch (error) {
        this.$message.error('加载十大流通股东数据失败: ' + error.message)
      } finally {
        this.loadingFree = false
      }
    },

    /**
     * 加载十大股东数据
     */
    async loadHolderData() {
      this.loadingHolder = true
      try {
        const response = await stockApi.getTop10Holders(this.queryForm.symbol, this.queryForm.date)
        this.holderList = response.data || []
        // 渲染股东饼图
        this.renderHolderPieChart()
      } catch (error) {
        this.$message.error('加载十大股东数据失败: ' + error.message)
      } finally {
        this.loadingHolder = false
      }
    },

    /**
     * 渲染十大流通股东饼图
     */
    renderFreeHolderPieChart() {
      if (!this.freeHolderList || this.freeHolderList.length === 0) {
        return
      }
      // 准备饼图数据
      const pieData = this.freeHolderList
        .filter(item => parseFloat(item['持股比例']) > 0)
        .map(item => ({
          name: item['股东名称'],
          value: parseFloat(item['持股比例']) || 0
        }))
      // 计算其他股东持股比例
      const totalRatio = pieData.reduce((sum, item) => sum + item.value, 0)
      if (totalRatio < 100) {
        pieData.push({
          name: '其他股东',
          value: parseFloat((100 - totalRatio).toFixed(2))
        })
      }
      // 配置饼图选项
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}% ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 10,
          top: 20,
          bottom: 20,
          textStyle: {
            fontSize: 11
          }
        },
        series: [
          {
            name: '持股比例',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: pieData
          }
        ]
      }
      this.freeHolderPieChart.setOption(option)
    },

    /**
     * 渲染十大股东饼图
     */
    renderHolderPieChart() {
      if (!this.holderList || this.holderList.length === 0) {
        return
      }
      // 准备饼图数据
      const pieData = this.holderList
        .filter(item => parseFloat(item['持股比例']) > 0)
        .map(item => ({
          name: item['股东名称'],
          value: parseFloat(item['持股比例']) || 0
        }))
      // 计算其他股东持股比例
      const totalRatio = pieData.reduce((sum, item) => sum + item.value, 0)
      if (totalRatio < 100) {
        pieData.push({
          name: '其他股东',
          value: parseFloat((100 - totalRatio).toFixed(2))
        })
      }
      // 配置饼图选项
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c}% ({d}%)'
        },
        legend: {
          type: 'scroll',
          orient: 'vertical',
          right: 10,
          top: 20,
          bottom: 20,
          textStyle: {
            fontSize: 11
          }
        },
        series: [
          {
            name: '持股比例',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['35%', '50%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 10,
              borderColor: '#fff',
              borderWidth: 2
            },
            label: {
              show: false,
              position: 'center'
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 16,
                fontWeight: 'bold'
              }
            },
            labelLine: {
              show: false
            },
            data: pieData
          }
        ]
      }
      this.holderPieChart.setOption(option)
    },

    /**
     * 格式化数字，添加千分位分隔符
     * @param {number} num - 需要格式化的数字
     * @returns {string} 格式化后的字符串
     */
    formatNumber(num) {
      if (!num) return '-'
      return parseFloat(num).toLocaleString('zh-CN')
    },

    /**
     * 格式化百分比
     * @param {number} val - 百分比数值
     * @returns {string} 格式化后的百分比字符串
     */
    formatPercent(val) {
      if (!val) return '-'
      const num = parseFloat(val)
      if (num > 0) return '+' + num.toFixed(2) + '%'
      return num.toFixed(2) + '%'
    },

    /**
     * 格式化持股变动
     * @param {number} val - 变动数值
     * @returns {string} 格式化后的变动字符串
     */
    formatChange(val) {
      if (!val) return '-'
      const num = parseFloat(val)
      if (num > 0) return '+' + this.formatNumber(num)
      return this.formatNumber(num)
    },

    /**
     * 获取变动样式类名
     * @param {number} val - 变动数值
     * @returns {string} 样式类名
     */
    getChangeClass(val) {
      if (!val) return ''
      const num = parseFloat(val)
      if (num > 0) return 'text-positive'
      if (num < 0) return 'text-negative'
      return ''
    }
  }
}
</script>

<style scoped>
.stock-holder {
  padding: 0;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}

.query-form {
  margin-bottom: 20px;
}

.chart-row {
  margin-bottom: 20px;
}

.pie-chart {
  width: 100%;
  height: 350px;
}

.table-card {
  margin-bottom: 20px;
}

.text-positive {
  color: #f56c6c;
}

.text-negative {
  color: #67c23a;
}
</style>
