<template>
  <div class="stock-history">
    <el-card>
      <div slot="header" class="clearfix">
        <span>历史K线图</span>
      </div>

      <!-- 查询条件 -->
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="股票代码">
          <el-input v-model="queryForm.symbol" placeholder="请输入股票代码" clearable></el-input>
        </el-form-item>
        <el-form-item label="周期">
          <el-select v-model="queryForm.period" placeholder="请选择周期">
            <el-option-group label="日K线">
              <el-option label="日线" value="daily"></el-option>
              <el-option label="周线" value="weekly"></el-option>
              <el-option label="月线" value="monthly"></el-option>
              <el-option label="季线" value="quarterly"></el-option>
              <el-option label="年线" value="yearly"></el-option>
            </el-option-group>
            <el-option-group label="分钟K线">
              <el-option label="1分钟" value="1"></el-option>
              <el-option label="5分钟" value="5"></el-option>
              <el-option label="15分钟" value="15"></el-option>
              <el-option label="30分钟" value="30"></el-option>
              <el-option label="60分钟" value="60"></el-option>
            </el-option-group>
          </el-select>
        </el-form-item>
        <el-form-item label="复权">
          <el-select v-model="queryForm.adjust" placeholder="请选择复权">
            <el-option label="前复权" value="qfq"></el-option>
            <el-option label="后复权" value="hfq"></el-option>
            <el-option label="不复权" value="none"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker
            v-model="queryForm.startDate"
            type="date"
            placeholder="选择开始日期"
            value-format="yyyyMMdd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker
            v-model="queryForm.endDate"
            type="date"
            placeholder="选择结束日期"
            value-format="yyyyMMdd"
          ></el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- K线图 -->
      <div ref="klineChart" style="width: 100%; height: 500px;"></div>

      <!-- 数据表格 -->
      <el-table
        :data="tableData"
        stripe
        style="width: 100%; margin-top: 20px;"
        height="300"
        v-loading="loading"
      >
        <el-table-column prop="日期" label="日期" width="120"></el-table-column>
        <el-table-column prop="开盘" label="开盘" width="100"></el-table-column>
        <el-table-column prop="收盘" label="收盘" width="100"></el-table-column>
        <el-table-column prop="最高" label="最高" width="100"></el-table-column>
        <el-table-column prop="最低" label="最低" width="100"></el-table-column>
        <el-table-column prop="成交量" label="成交量" width="120"></el-table-column>
        <el-table-column prop="成交额" label="成交额" width="120"></el-table-column>
        <el-table-column prop="涨跌幅" label="涨跌幅" width="100">
          <template slot-scope="scope">
            <span :style="{ color: getPriceColor(scope.row['涨跌幅']) }">
              {{ scope.row['涨跌幅'] ? scope.row['涨跌幅'].toFixed(2) + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="换手率" label="换手率" width="100">
          <template slot-scope="scope">
            {{ scope.row['换手率'] ? scope.row['换手率'].toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { stockApi } from '../utils/api'

export default {
  name: 'StockHistory',
  data() {
    return {
      queryForm: {
        symbol: '000001',
        period: 'daily',
        adjust: 'qfq',
        startDate: '',
        endDate: ''
      },
      tableData: [],
      loading: false,
      klineChart: null
    }
  },
  mounted() {
    this.initChart()
    this.loadData()
  },
  beforeDestroy() {
    if (this.klineChart) {
      this.klineChart.dispose()
    }
  },
  methods: {
    initChart() {
      this.klineChart = echarts.init(this.$refs.klineChart)
      window.addEventListener('resize', () => {
        this.klineChart.resize()
      })
    },
    async loadData() {
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }

      this.loading = true
      try {
        // 判断是否为分钟K线（值为数字字符串）
        const isMinuteKline = ['1', '5', '15', '30', '60'].includes(this.queryForm.period)
        
        let response
        if (isMinuteKline) {
          // 分钟K线使用单独的API
          const params = {
            symbol: this.queryForm.symbol,
            period: parseInt(this.queryForm.period),
            adjust: this.queryForm.adjust
          }
          response = await stockApi.getMinuteHistory(params)
        } else {
          // 日K线使用历史数据API
          const params = {
            symbol: this.queryForm.symbol,
            period: this.queryForm.period,
            adjust: this.queryForm.adjust
          }
          if (this.queryForm.startDate) {
            params.startDate = this.queryForm.startDate
          }
          if (this.queryForm.endDate) {
            params.endDate = this.queryForm.endDate
          }
          response = await stockApi.getHistory(params)
        }
        
        this.tableData = response.data || []
        this.renderChart()
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    renderChart() {
      if (!this.tableData || this.tableData.length === 0) {
        return
      }

      const dates = this.tableData.map(item => item['日期'])
      const values = this.tableData.map(item => [
        item['开盘'],
        item['收盘'],
        item['最低'],
        item['最高']
      ])
      const volumes = this.tableData.map(item => item['成交量'])

      // 获取周期中文名称
      const periodMap = {
        'daily': '日线',
        'weekly': '周线',
        'monthly': '月线',
        'quarterly': '季线',
        'yearly': '年线',
        '1': '1分钟',
        '5': '5分钟',
        '15': '15分钟',
        '30': '30分钟',
        '60': '60分钟'
      }
      const periodName = periodMap[this.queryForm.period] || 'K线'
      
      const option = {
        title: {
          text: this.queryForm.symbol + ' ' + periodName,
          left: 'center'
        },
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'cross'
          }
        },
        legend: {
          data: ['K线', '成交量'],
          top: 30
        },
        grid: [
          {
            left: '10%',
            right: '8%',
            top: 80,
            height: '50%'
          },
          {
            left: '10%',
            right: '8%',
            top: '70%',
            height: '15%'
          }
        ],
        xAxis: [
          {
            type: 'category',
            data: dates,
            scale: true,
            boundaryGap: false,
            axisLine: { onZero: false },
            splitLine: { show: false },
            min: 'dataMin',
            max: 'dataMax'
          },
          {
            type: 'category',
            gridIndex: 1,
            data: dates,
            scale: true,
            boundaryGap: false,
            axisLine: { onZero: false },
            axisTick: { show: false },
            splitLine: { show: false },
            axisLabel: { show: false },
            min: 'dataMin',
            max: 'dataMax'
          }
        ],
        yAxis: [
          {
            scale: true,
            splitArea: {
              show: true
            }
          },
          {
            scale: true,
            gridIndex: 1,
            splitNumber: 2,
            axisLabel: { show: false },
            axisLine: { show: false },
            axisTick: { show: false },
            splitLine: { show: false }
          }
        ],
        dataZoom: [
          {
            type: 'inside',
            xAxisIndex: [0, 1],
            start: 50,
            end: 100
          },
          {
            show: true,
            xAxisIndex: [0, 1],
            type: 'slider',
            bottom: 10,
            start: 50,
            end: 100
          }
        ],
        series: [
          {
            name: 'K线',
            type: 'candlestick',
            data: values,
            itemStyle: {
              color: '#ef5350',
              color0: '#26a69a',
              borderColor: '#ef5350',
              borderColor0: '#26a69a'
            }
          },
          {
            name: '成交量',
            type: 'bar',
            xAxisIndex: 1,
            yAxisIndex: 1,
            data: volumes,
            itemStyle: {
              color: function(params) {
                var dataList = params.dataIndex
                var value = values[dataList]
                if (value[1] > value[0]) {
                  return '#ef5350'
                } else {
                  return '#26a69a'
                }
              }
            }
          }
        ]
      }

      this.klineChart.setOption(option)
    },
    getPriceColor(changePercent) {
      if (!changePercent) return '#303133'
      if (changePercent > 0) return '#f56c6c'
      if (changePercent < 0) return '#67c23a'
      return '#303133'
    }
  }
}
</script>

<style scoped>
.stock-history {
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
.demo-form-inline {
  margin-bottom: 20px;
}
</style>
