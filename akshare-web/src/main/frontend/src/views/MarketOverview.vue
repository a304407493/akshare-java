<template>
  <div class="market-overview">
    <el-row :gutter="20">
      <!-- 上交所概况 -->
      <el-col :span="12">
        <el-card>
          <div slot="header" class="clearfix">
            <span>上海证券交易所概况</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadSseData">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table :data="sseData" style="width: 100%" v-loading="sseLoading">
            <el-table-column prop="类别" label="类别" width="150"></el-table-column>
            <el-table-column prop="上市公司数量" label="上市公司数量"></el-table-column>
            <el-table-column prop="总市值(亿元)" label="总市值(亿元)"></el-table-column>
            <el-table-column prop="流通市值(亿元)" label="流通市值(亿元)"></el-table-column>
            <el-table-column prop="平均市盈率" label="平均市盈率"></el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <!-- 深交所概况 -->
      <el-col :span="12">
        <el-card>
          <div slot="header" class="clearfix">
            <span>深圳证券交易所概况</span>
            <el-button style="float: right; padding: 3px 0" type="text" @click="loadSzseData">
              <i class="el-icon-refresh"></i> 刷新
            </el-button>
          </div>
          <el-table :data="szseData" style="width: 100%" v-loading="szseLoading">
            <el-table-column prop="类别" label="类别" width="150"></el-table-column>
            <el-table-column prop="上市公司数量" label="上市公司数量"></el-table-column>
            <el-table-column prop="总市值(亿元)" label="总市值(亿元)"></el-table-column>
            <el-table-column prop="流通市值(亿元)" label="流通市值(亿元)"></el-table-column>
            <el-table-column prop="平均市盈率" label="平均市盈率"></el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 个股信息查询 -->
    <el-card style="margin-top: 20px;">
      <div slot="header" class="clearfix">
        <span>个股基础资料查询</span>
      </div>
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="股票代码">
          <el-input v-model="queryForm.symbol" placeholder="请输入股票代码" clearable></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadStockInfo">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="stockInfoData" style="width: 100%" v-loading="stockInfoLoading">
        <el-table-column prop="股票代码" label="股票代码" width="120"></el-table-column>
        <el-table-column prop="股票简称" label="股票简称" width="120"></el-table-column>
        <el-table-column prop="上市日期" label="上市日期" width="120"></el-table-column>
        <el-table-column prop="所属行业" label="所属行业" width="120"></el-table-column>
        <el-table-column prop="主营业务" label="主营业务"></el-table-column>
        <el-table-column prop="总股本(亿股)" label="总股本(亿股)" width="140"></el-table-column>
        <el-table-column prop="流通股本(亿股)" label="流通股本(亿股)" width="140"></el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'

export default {
  name: 'MarketOverview',
  data() {
    return {
      sseData: [],
      szseData: [],
      sseLoading: false,
      szseLoading: false,
      queryForm: {
        symbol: ''
      },
      stockInfoData: [],
      stockInfoLoading: false
    }
  },
  mounted() {
    this.loadSseData()
    this.loadSzseData()
  },
  methods: {
    async loadSseData() {
      this.sseLoading = true
      try {
        const response = await stockApi.getSseSummary()
        this.sseData = response.data || []
      } catch (error) {
        this.$message.error('加载上交所数据失败: ' + error.message)
      } finally {
        this.sseLoading = false
      }
    },
    async loadSzseData() {
      this.szseLoading = true
      try {
        const response = await stockApi.getSzseSummary()
        this.szseData = response.data || []
      } catch (error) {
        this.$message.error('加载深交所数据失败: ' + error.message)
      } finally {
        this.szseLoading = false
      }
    },
    async loadStockInfo() {
      if (!this.queryForm.symbol) {
        this.$message.warning('请输入股票代码')
        return
      }
      this.stockInfoLoading = true
      try {
        const response = await stockApi.getStockInfo(this.queryForm.symbol)
        this.stockInfoData = response.data || []
      } catch (error) {
        this.$message.error('加载个股信息失败: ' + error.message)
      } finally {
        this.stockInfoLoading = false
      }
    }
  }
}
</script>

<style scoped>
.market-overview {
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
