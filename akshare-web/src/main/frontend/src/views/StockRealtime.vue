<template>
  <div class="stock-realtime">
    <el-card>
      <div slot="header" class="clearfix">
        <span>A股实时行情</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="refreshData">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="东方财富" name="em"></el-tab-pane>
        <el-tab-pane label="新浪财经" name="sina"></el-tab-pane>
      </el-tabs>

      <el-table
        :data="tableData"
        stripe
        style="width: 100%; margin-top: 20px;"
        height="600"
        v-loading="loading"
      >
        <el-table-column prop="代码" label="代码" width="100" fixed></el-table-column>
        <el-table-column prop="名称" label="名称" width="120" fixed></el-table-column>
        <el-table-column prop="最新价" label="最新价" width="100">
          <template slot-scope="scope">
            <span :style="{ color: getPriceColor(scope.row['涨跌幅']) }">{{ scope.row['最新价'] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="涨跌幅" label="涨跌幅" width="100">
          <template slot-scope="scope">
            <span :style="{ color: getPriceColor(scope.row['涨跌幅']) }">
              {{ scope.row['涨跌幅'] ? scope.row['涨跌幅'].toFixed(2) + '%' : '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="涨跌额" label="涨跌额" width="100">
          <template slot-scope="scope">
            <span :style="{ color: getPriceColor(scope.row['涨跌幅']) }">{{ scope.row['涨跌额'] }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="今开" label="今开" width="100"></el-table-column>
        <el-table-column prop="最高" label="最高" width="100"></el-table-column>
        <el-table-column prop="最低" label="最低" width="100"></el-table-column>
        <el-table-column prop="成交量" label="成交量" width="120"></el-table-column>
        <el-table-column prop="成交额" label="成交额" width="120"></el-table-column>
        <el-table-column prop="换手率" label="换手率" width="100">
          <template slot-scope="scope">
            {{ scope.row['换手率'] ? scope.row['换手率'].toFixed(2) + '%' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="市盈率" label="市盈率" width="100"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

export default {
  name: 'StockRealtime',
  data() {
    return {
      activeTab: 'em',
      tableData: [],
      loading: false
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    handleTabClick(tab) {
      this.loadData()
    },
    refreshData() {
      this.loadData()
    },
    async loadData() {
      this.loading = true
      try {
        let response
        if (this.activeTab === 'em') {
          response = await stockApi.getRealtimeEm()
        } else {
          response = await stockApi.getRealtimeSina()
        }
        this.tableData = response.data || []
      } catch (error) {
        this.$message.error('加载数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
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
.stock-realtime {
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
</style>
