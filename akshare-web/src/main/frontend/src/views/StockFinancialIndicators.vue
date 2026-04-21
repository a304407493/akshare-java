<template>
  <!-- 财务指标页面主容器 -->
  <div class="stock-financial-indicators">
    <el-card>
      <!-- 卡片头部：标题和刷新按钮 -->
      <div slot="header" class="clearfix">
        <span>财务指标分析</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="loadFinancialAnalysis">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <!-- 查询条件表单 -->
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <!-- 股票代码输入框 -->
        <el-form-item label="股票代码">
          <el-input v-model="queryForm.symbol" placeholder="请输入股票代码" clearable></el-input>
        </el-form-item>
        <!-- 报表类型选择器 -->
        <el-form-item label="报表类型">
          <el-select v-model="queryForm.reportType" placeholder="请选择报表类型">
            <el-option label="按报告期" value="1"></el-option>
            <el-option label="按年度" value="2"></el-option>
            <el-option label="按单季度" value="3"></el-option>
          </el-select>
        </el-form-item>
        <!-- 查询按钮 -->
        <el-form-item>
          <el-button type="primary" @click="loadFinancialAnalysis">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 财务指标标签页 -->
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 盈利能力指标标签页 -->
        <el-tab-pane label="盈利能力" name="profitability">
          <div ref="profitabilityChart" class="chart-container"></div>
        </el-tab-pane>

        <!-- 偿债能力指标标签页 -->
        <el-tab-pane label="偿债能力" name="solvency">
          <div ref="solvencyChart" class="chart-container"></div>
        </el-tab-pane>

        <!-- 成长能力指标标签页 -->
        <el-tab-pane label="成长能力" name="growth">
          <div ref="growthChart" class="chart-container"></div>
        </el-tab-pane>

        <!-- 营运能力指标标签页 -->
        <el-tab-pane label="营运能力" name="operation">
          <div ref="operationChart" class="chart-container"></div>
        </el-tab-pane>

        <!-- 综合财务能力雷达图标签页 -->
        <el-tab-pane label="综合财务能力" name="radar">
          <div ref="radarChart" class="chart-container"></div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
// 导入ECharts图表库
import * as echarts from 'echarts'
// 导入财务数据相关API
import { financialApi } from '../utils/api'

export default {
  name: 'StockFinancialIndicators',
  data() {
    return {
      // 查询表单数据
      queryForm: {
        symbol: '000001', // 默认股票代码
        reportType: '1'   // 默认按报告期
      },
      // 当前激活的标签页
      activeTab: 'profitability',
      // 财务分析指标数据
      financialData: [],
      // 图表实例对象
      charts: {
        profitability: null, // 盈利能力图表实例
        solvency: null,      // 偿债能力图表实例
        growth: null,        // 成长能力图表实例
        operation: null,     // 营运能力图表实例
        radar: null          // 雷达图实例
      }
    }
  },
  watch: {
    // 监听标签页切换，切换时重新渲染对应图表
    activeTab(newVal) {
      this.$nextTick(() => {
        this.renderChart(newVal)
      })
    }
  },
  mounted() {
    // 组件挂载时初始化图表并加载数据
    this.initCharts()
    this.loadFinancialAnalysis()
    // 监听窗口大小变化，自适应调整图表大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    // 组件销毁前清理图表实例和事件监听
    window.removeEventListener('resize', this.handleResize)
    Object.values(this.charts).forEach(chart => {
      if (chart) {
        chart.dispose()
      }
    })
  },
  methods: {
    /**
     * 初始化所有图表实例
     * 为每个指标类型创建ECharts实例
     */
    initCharts() {
      // 初始化盈利能力图表
      this.charts.profitability = echarts.init(this.$refs.profitabilityChart)
      // 初始化偿债能力图表
      this.charts.solvency = echarts.init(this.$refs.solvencyChart)
      // 初始化成长能力图表
      this.charts.growth = echarts.init(this.$refs.growthChart)
      // 初始化营运能力图表
      this.charts.operation = echarts.init(this.$refs.operationChart)
      // 初始化雷达图
      this.charts.radar = echarts.init(this.$refs.radarChart)
    },

    /**
     * 处理窗口大小变化事件
     * 调整所有图表的大小以适应容器
     */
    handleResize() {
      Object.values(this.charts).forEach(chart => {
        if (chart) {
          chart.resize()
        }
      })
    },

    /**
     * 加载财务分析指标数据
     * 调用后端API获取财务分析指标数据
     */
    async loadFinancialAnalysis() {
      // 验证股票代码
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }

      try {
        const params = {
          symbol: this.queryForm.symbol,
          reportType: this.queryForm.reportType
        }
        const response = await financialApi.getFinancialAnalysis(params)
        this.financialData = response.data || []
        // 数据加载完成后渲染当前标签页的图表
        this.$nextTick(() => {
          this.renderChart(this.activeTab)
        })
      } catch (error) {
        this.$message.error('加载财务分析指标数据失败: ' + error.message)
      }
    },

    /**
     * 渲染指定类型的图表
     * @param {String} chartType - 图表类型：profitability/solvency/growth/operation/radar
     */
    renderChart(chartType) {
      // 检查数据是否为空
      if (!this.financialData || this.financialData.length === 0) {
        return
      }

      // 根据图表类型调用对应的渲染方法
      switch (chartType) {
        case 'profitability':
          this.renderProfitabilityChart()
          break
        case 'solvency':
          this.renderSolvencyChart()
          break
        case 'growth':
          this.renderGrowthChart()
          break
        case 'operation':
          this.renderOperationChart()
          break
        case 'radar':
          this.renderRadarChart()
          break
      }
    },

    /**
     * 渲染盈利能力指标图表
     * 展示ROE、ROA、毛利率、净利率的趋势
     */
    renderProfitabilityChart() {
      // 提取报告期作为X轴数据
      const dates = this.financialData.map(item => item['报告期']).reverse()
      // 提取各项盈利能力指标数据
      const roeData = this.financialData.map(item => this.parseNumber(item['净资产收益率'])).reverse()
      const roaData = this.financialData.map(item => this.parseNumber(item['总资产报酬率'])).reverse()
      const grossMarginData = this.financialData.map(item => this.parseNumber(item['销售毛利率'])).reverse()
      const netMarginData = this.financialData.map(item => this.parseNumber(item['销售净利率'])).reverse()

      // ECharts配置选项
      const option = {
        title: {
          text: '盈利能力指标趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['净资产收益率(ROE)', '总资产报酬率(ROA)', '销售毛利率', '销售净利率'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 80,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { rotate: 45 }
        },
        yAxis: {
          type: 'value',
          name: '百分比(%)',
          axisLabel: { formatter: '{value}%' }
        },
        series: [
          {
            name: '净资产收益率(ROE)',
            type: 'line',
            data: roeData,
            smooth: true,
            itemStyle: { color: '#5470c6' }
          },
          {
            name: '总资产报酬率(ROA)',
            type: 'line',
            data: roaData,
            smooth: true,
            itemStyle: { color: '#91cc75' }
          },
          {
            name: '销售毛利率',
            type: 'line',
            data: grossMarginData,
            smooth: true,
            itemStyle: { color: '#fac858' }
          },
          {
            name: '销售净利率',
            type: 'line',
            data: netMarginData,
            smooth: true,
            itemStyle: { color: '#ee6666' }
          }
        ]
      }

      this.charts.profitability.setOption(option)
    },

    /**
     * 渲染偿债能力指标图表
     * 展示资产负债率、流动比率、速动比率的趋势
     */
    renderSolvencyChart() {
      // 提取报告期作为X轴数据
      const dates = this.financialData.map(item => item['报告期']).reverse()
      // 提取各项偿债能力指标数据
      const debtRatioData = this.financialData.map(item => this.parseNumber(item['资产负债率'])).reverse()
      const currentRatioData = this.financialData.map(item => this.parseNumber(item['流动比率'])).reverse()
      const quickRatioData = this.financialData.map(item => this.parseNumber(item['速动比率'])).reverse()

      // ECharts配置选项
      const option = {
        title: {
          text: '偿债能力指标趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['资产负债率', '流动比率', '速动比率'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 80,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { rotate: 45 }
        },
        yAxis: [
          {
            type: 'value',
            name: '资产负债率(%)',
            position: 'left',
            axisLabel: { formatter: '{value}%' }
          },
          {
            type: 'value',
            name: '比率',
            position: 'right',
            axisLabel: { formatter: '{value}' }
          }
        ],
        series: [
          {
            name: '资产负债率',
            type: 'line',
            yAxisIndex: 0,
            data: debtRatioData,
            smooth: true,
            itemStyle: { color: '#5470c6' }
          },
          {
            name: '流动比率',
            type: 'line',
            yAxisIndex: 1,
            data: currentRatioData,
            smooth: true,
            itemStyle: { color: '#91cc75' }
          },
          {
            name: '速动比率',
            type: 'line',
            yAxisIndex: 1,
            data: quickRatioData,
            smooth: true,
            itemStyle: { color: '#fac858' }
          }
        ]
      }

      this.charts.solvency.setOption(option)
    },

    /**
     * 渲染成长能力指标图表
     * 展示营收增长率、净利润增长率的趋势
     */
    renderGrowthChart() {
      // 提取报告期作为X轴数据
      const dates = this.financialData.map(item => item['报告期']).reverse()
      // 提取各项成长能力指标数据
      const revenueGrowthData = this.financialData.map(item => this.parseNumber(item['营业收入增长率'])).reverse()
      const profitGrowthData = this.financialData.map(item => this.parseNumber(item['净利润增长率'])).reverse()
      const totalAssetGrowthData = this.financialData.map(item => this.parseNumber(item['总资产增长率'])).reverse()

      // ECharts配置选项
      const option = {
        title: {
          text: '成长能力指标趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['营业收入增长率', '净利润增长率', '总资产增长率'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 80,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { rotate: 45 }
        },
        yAxis: {
          type: 'value',
          name: '增长率(%)',
          axisLabel: { formatter: '{value}%' }
        },
        series: [
          {
            name: '营业收入增长率',
            type: 'bar',
            data: revenueGrowthData,
            itemStyle: { color: '#5470c6' }
          },
          {
            name: '净利润增长率',
            type: 'bar',
            data: profitGrowthData,
            itemStyle: { color: '#91cc75' }
          },
          {
            name: '总资产增长率',
            type: 'line',
            data: totalAssetGrowthData,
            smooth: true,
            itemStyle: { color: '#fac858' }
          }
        ]
      }

      this.charts.growth.setOption(option)
    },

    /**
     * 渲染营运能力指标图表
     * 展示总资产周转率、存货周转率的趋势
     */
    renderOperationChart() {
      // 提取报告期作为X轴数据
      const dates = this.financialData.map(item => item['报告期']).reverse()
      // 提取各项营运能力指标数据
      const totalAssetTurnoverData = this.financialData.map(item => this.parseNumber(item['总资产周转率'])).reverse()
      const inventoryTurnoverData = this.financialData.map(item => this.parseNumber(item['存货周转率'])).reverse()
      const receivableTurnoverData = this.financialData.map(item => this.parseNumber(item['应收账款周转率'])).reverse()

      // ECharts配置选项
      const option = {
        title: {
          text: '营运能力指标趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: { type: 'cross' }
        },
        legend: {
          data: ['总资产周转率', '存货周转率', '应收账款周转率'],
          top: 30
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: 80,
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: { rotate: 45 }
        },
        yAxis: {
          type: 'value',
          name: '周转率(次)',
          axisLabel: { formatter: '{value}' }
        },
        series: [
          {
            name: '总资产周转率',
            type: 'line',
            data: totalAssetTurnoverData,
            smooth: true,
            itemStyle: { color: '#5470c6' }
          },
          {
            name: '存货周转率',
            type: 'line',
            data: inventoryTurnoverData,
            smooth: true,
            itemStyle: { color: '#91cc75' }
          },
          {
            name: '应收账款周转率',
            type: 'line',
            data: receivableTurnoverData,
            smooth: true,
            itemStyle: { color: '#fac858' }
          }
        ]
      }

      this.charts.operation.setOption(option)
    },

    /**
     * 渲染综合财务能力雷达图
     * 展示各项财务指标的综合评分
     */
    renderRadarChart() {
      // 获取最新一期的数据用于雷达图展示
      const latestData = this.financialData[0]
      if (!latestData) {
        return
      }

      // 提取各项指标的最新值
      const indicatorValues = [
        this.parseNumber(latestData['净资产收益率']),      // ROE
        this.parseNumber(latestData['总资产报酬率']),      // ROA
        this.parseNumber(latestData['销售毛利率']),        // 毛利率
        this.parseNumber(latestData['销售净利率']),        // 净利率
        this.parseNumber(latestData['营业收入增长率']),    // 营收增长率
        this.parseNumber(latestData['净利润增长率']),      // 净利润增长率
        this.parseNumber(latestData['总资产周转率']),      // 总资产周转率
        this.parseNumber(latestData['存货周转率'])         // 存货周转率
      ]

      // ECharts雷达图配置选项
      const option = {
        title: {
          text: '综合财务能力雷达图',
          subtext: `报告期: ${latestData['报告期']}`,
          left: 'center'
        },
        tooltip: {
          trigger: 'item'
        },
        legend: {
          data: ['财务指标'],
          bottom: 10
        },
        radar: {
          indicator: [
            { name: '净资产收益率(ROE)', max: 30 },
            { name: '总资产报酬率(ROA)', max: 20 },
            { name: '销售毛利率', max: 50 },
            { name: '销售净利率', max: 30 },
            { name: '营业收入增长率', max: 50 },
            { name: '净利润增长率', max: 50 },
            { name: '总资产周转率', max: 2 },
            { name: '存货周转率', max: 10 }
          ],
          shape: 'polygon',
          splitNumber: 5,
          axisName: {
            color: '#333',
            fontSize: 12
          },
          splitLine: {
            lineStyle: {
              color: ['#eee']
            }
          },
          splitArea: {
            show: true,
            areaStyle: {
              color: ['#f8f8f8', '#fff']
            }
          }
        },
        series: [
          {
            name: '财务指标',
            type: 'radar',
            data: [
              {
                value: indicatorValues,
                name: '财务指标',
                areaStyle: {
                  color: 'rgba(84, 112, 198, 0.3)'
                },
                lineStyle: {
                  color: '#5470c6',
                  width: 2
                },
                itemStyle: {
                  color: '#5470c6'
                }
              }
            ]
          }
        ]
      }

      this.charts.radar.setOption(option)
    },

    /**
     * 解析数值字符串为数字
     * 处理可能包含百分号或其他格式的数值
     * @param {String|Number} value - 需要解析的数值
     * @returns {Number} 解析后的数字
     */
    parseNumber(value) {
      if (value === null || value === undefined || value === '-') {
        return 0
      }
      // 如果值是字符串且包含百分号，去掉百分号并转换为数字
      if (typeof value === 'string') {
        if (value.includes('%')) {
          return parseFloat(value.replace('%', ''))
        }
      }
      const num = parseFloat(value)
      return isNaN(num) ? 0 : num
    }
  }
}
</script>

<style scoped>
/* 页面主容器样式 */
.stock-financial-indicators {
  padding: 0;
}

/* 清除浮动样式 */
.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both;
}

/* 查询表单样式 */
.demo-form-inline {
  margin-bottom: 20px;
}

/* 图表容器样式 */
.chart-container {
  width: 100%;
  height: 500px;
}
</style>
