<template>
  <div class="stock-holder-trend">
    <el-card>
      <div slot="header" class="clearfix">
        <span>股东趋势分析</span>
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

      <!-- 股东户数变化趋势折线图 -->
      <el-card class="chart-card">
        <div slot="header">
          <span>股东户数变化趋势</span>
          <el-tooltip content="展示报告期内股东户数的变化情况，反映筹码集中度" placement="top">
            <i class="el-icon-question" style="margin-left: 5px; color: #909399;"></i>
          </el-tooltip>
        </div>
        <div ref="holderCountChart" class="trend-chart"></div>
      </el-card>

      <!-- 股东持股变动柱状图 -->
      <el-card class="chart-card">
        <div slot="header">
          <span>股东持股变动分析</span>
          <el-tooltip content="展示各股东的持股数量变动情况" placement="top">
            <i class="el-icon-question" style="margin-left: 5px; color: #909399;"></i>
          </el-tooltip>
        </div>
        <div ref="holdingChangeChart" class="trend-chart"></div>
      </el-card>

      <!-- 持股详情数据表格 -->
      <el-card class="table-card">
        <div slot="header">
          <span>持股详情</span>
        </div>
        <el-table :data="holdingDetailList" stripe style="width: 100%" v-loading="loading">
          <el-table-column prop="报告期" label="报告期" width="120"></el-table-column>
          <el-table-column prop="股东户数" label="股东户数" width="150">
            <template slot-scope="scope">
              {{ formatNumber(scope.row['股东户数']) }}
            </template>
          </el-table-column>
          <el-table-column prop="户均持股" label="户均持股" width="150">
            <template slot-scope="scope">
              {{ formatNumber(scope.row['户均持股']) }}
            </template>
          </el-table-column>
          <el-table-column prop="较上期变化" label="较上期变化" width="120">
            <template slot-scope="scope">
              <span :class="getChangeClass(scope.row['较上期变化'])">
                {{ formatPercent(scope.row['较上期变化']) }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="筹码集中度" label="筹码集中度" width="150">
            <template slot-scope="scope">
              <el-progress :percentage="parseFloat(scope.row['筹码集中度']) || 0" :color="progressColors"></el-progress>
            </template>
          </el-table-column>
          <el-table-column prop="人均持股" label="人均持股" width="150">
            <template slot-scope="scope">
              {{ formatNumber(scope.row['人均持股']) }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { stockApi } from '../utils/api'

export default {
  name: 'StockHolderTrend',
  data() {
    return {
      // 查询表单数据
      queryForm: {
        symbol: '000001',
        date: '20240930'  // 默认报告期日期
      },
      // 持股详情列表数据
      holdingDetailList: [],
      // 流通持股详情列表数据
      freeHoldingDetailList: [],
      // 加载状态
      loading: false,
      // 图表实例
      holderCountChart: null,
      holdingChangeChart: null,
      // 进度条颜色配置
      progressColors: [
        { color: '#67c23a', percentage: 30 },
        { color: '#e6a23c', percentage: 60 },
        { color: '#f56c6c', percentage: 100 }
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
    if (this.holderCountChart) {
      this.holderCountChart.dispose()
    }
    if (this.holdingChangeChart) {
      this.holdingChangeChart.dispose()
    }
    // 移除事件监听
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    /**
     * 初始化ECharts图表实例
     */
    initCharts() {
      this.holderCountChart = echarts.init(this.$refs.holderCountChart)
      this.holdingChangeChart = echarts.init(this.$refs.holdingChangeChart)
    },

    /**
     * 处理窗口大小变化，调整图表尺寸
     */
    handleResize() {
      if (this.holderCountChart) {
        this.holderCountChart.resize()
      }
      if (this.holdingChangeChart) {
        this.holdingChangeChart.resize()
      }
    },

    /**
     * 加载股东趋势数据
     */
    async loadData() {
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }
      // 并行加载股东户数和流通持股详情数据
      await Promise.all([
        this.loadHolderNum(),
        this.loadFreeHoldingDetail()
      ])
    },

    /**
     * 加载股东户数数据
     */
    async loadHolderNum() {
      this.loading = true
      try {
        const response = await stockApi.getHolderNum(this.queryForm.symbol)
        this.holdingDetailList = response.data || []
        // 渲染股东户数趋势图
        this.renderHolderCountChart()
      } catch (error) {
        this.$message.error('加载股东户数数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    /**
     * 加载流通持股详情数据
     */
    async loadFreeHoldingDetail() {
      try {
        const response = await stockApi.getFreeHoldingDetail(this.queryForm.symbol, this.queryForm.date)
        this.freeHoldingDetailList = response.data || []
        // 渲染持股变动图
        this.renderHoldingChangeChart()
      } catch (error) {
        this.$message.error('加载流通持股详情失败: ' + error.message)
      }
    },

    /**
     * 渲染股东户数变化趋势折线图
     */
    renderHolderCountChart() {
      if (!this.holdingDetailList || this.holdingDetailList.length === 0) {
        return
      }
      // 按报告期排序（从早到晚）
      const sortedData = [...this.holdingDetailList].reverse()
      // 提取数据
      const periods = sortedData.map(item => item['报告期'])
      const holderCounts = sortedData.map(item => parseFloat(item['股东户数']) || 0)
      const avgHoldings = sortedData.map(item => parseFloat(item['户均持股']) || 0)
      // 配置折线图选项
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['股东户数', '户均持股'],
          top: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: periods,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: [
          {
            type: 'value',
            name: '股东户数',
            position: 'left',
            axisLabel: {
              formatter: function(value) {
                if (value >= 10000) {
                  return (value / 10000).toFixed(1) + '万'
                }
                return value
              }
            }
          },
          {
            type: 'value',
            name: '户均持股',
            position: 'right',
            axisLabel: {
              formatter: function(value) {
                if (value >= 10000) {
                  return (value / 10000).toFixed(1) + '万'
                }
                return value
              }
            }
          }
        ],
        series: [
          {
            name: '股东户数',
            type: 'line',
            data: holderCounts,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3,
              color: '#409eff'
            },
            itemStyle: {
              color: '#409eff'
            },
            areaStyle: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
                { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
              ])
            }
          },
          {
            name: '户均持股',
            type: 'line',
            yAxisIndex: 1,
            data: avgHoldings,
            smooth: true,
            symbol: 'circle',
            symbolSize: 8,
            lineStyle: {
              width: 3,
              color: '#67c23a'
            },
            itemStyle: {
              color: '#67c23a'
            }
          }
        ]
      }
      this.holderCountChart.setOption(option)
    },

    /**
     * 渲染股东持股变动柱状图
     */
    renderHoldingChangeChart() {
      if (!this.freeHoldingDetailList || this.freeHoldingDetailList.length === 0) {
        return
      }
      // 按报告期分组，计算每期持股变动
      const periodMap = new Map()
      this.freeHoldingDetailList.forEach(item => {
        const period = item['报告期']
        if (!periodMap.has(period)) {
          periodMap.set(period, { period, changes: [] })
        }
        const change = parseFloat(item['持股变动']) || 0
        if (change !== 0) {
          periodMap.get(period).changes.push(change)
        }
      })
      // 转换为数组并排序
      const periodData = Array.from(periodMap.values()).sort((a, b) => a.period.localeCompare(b.period))
      const periods = periodData.map(item => item.period)
      // 计算每期增持和减持数量
      const increaseData = periodData.map(item => {
        return item.changes.filter(c => c > 0).reduce((sum, c) => sum + c, 0)
      })
      const decreaseData = periodData.map(item => {
        return item.changes.filter(c => c < 0).reduce((sum, c) => sum + Math.abs(c), 0)
      })
      // 配置柱状图选项
      const option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          },
          formatter: function(params) {
            let result = params[0].name + '<br/>'
            params.forEach(param => {
              result += param.marker + param.seriesName + ': ' + param.value.toLocaleString() + '<br/>'
            })
            return result
          }
        },
        legend: {
          data: ['增持', '减持'],
          top: 10
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: periods,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '持股变动',
          axisLabel: {
            formatter: function(value) {
              if (value >= 100000000) {
                return (value / 100000000).toFixed(1) + '亿'
              } else if (value >= 10000) {
                return (value / 10000).toFixed(1) + '万'
              }
              return value
            }
          }
        },
        series: [
          {
            name: '增持',
            type: 'bar',
            stack: 'total',
            data: increaseData,
            itemStyle: {
              color: '#f56c6c'
            },
            barWidth: '50%'
          },
          {
            name: '减持',
            type: 'bar',
            stack: 'total',
            data: decreaseData.map(v => -v),
            itemStyle: {
              color: '#67c23a'
            },
            barWidth: '50%'
          }
        ]
      }
      this.holdingChangeChart.setOption(option)
    },

    /**
     * 格式化数字，添加千分位分隔符
     * @param {number} num - 需要格式化的数字
     * @returns {string} 格式化后的字符串
     */
    formatNumber(num) {
      if (!num) return '-'
      const value = parseFloat(num)
      if (value >= 100000000) {
        return (value / 100000000).toFixed(2) + '亿'
      } else if (value >= 10000) {
        return (value / 10000).toFixed(2) + '万'
      }
      return value.toLocaleString('zh-CN')
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
.stock-holder-trend {
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

.chart-card {
  margin-bottom: 20px;
}

.trend-chart {
  width: 100%;
  height: 400px;
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
