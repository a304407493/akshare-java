<template>
  <div class="stock-auction">
    <el-card>
      <div slot="header" class="clearfix">
        <span>集合竞价数据</span>
      </div>

      <div class="search-bar">
        <el-input
          v-model="stockCode"
          placeholder="请输入股票代码"
          style="width: 200px;"
          clearable
          @keyup.enter.native="handleQuery"
        ></el-input>
        <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        <el-button @click="refreshData" :loading="loading">
          <i class="el-icon-refresh"></i> 刷新
        </el-button>
      </div>

      <el-divider content-position="left">集合竞价数据</el-divider>
      <el-table
        :data="auctionData"
        stripe
        style="width: 100%;"
        v-loading="loading"
        max-height="400"
      >
        <el-table-column prop="时间" label="时间" width="150"></el-table-column>
        <el-table-column prop="最新价" label="最新价" width="120"></el-table-column>
        <el-table-column prop="均价" label="均价" width="120"></el-table-column>
        <el-table-column prop="最高" label="最高" width="120"></el-table-column>
        <el-table-column prop="最低" label="最低" width="120"></el-table-column>
        <el-table-column prop="成交量" label="成交量(手)" width="150"></el-table-column>
        <el-table-column prop="成交额" label="成交额(元)" width="150"></el-table-column>
      </el-table>

      <div class="bid-ask-container">
        <el-card class="bid-ask-card">
          <div slot="header">
            <span>买五档</span>
          </div>
          <el-table
            :data="bidData"
            style="width: 100%;"
            :show-header="false"
            v-loading="loading"
          >
            <el-table-column prop="price" width="120">
              <template slot-scope="scope">
                <span class="price-green">{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="volume" label="数量"></el-table-column>
          </el-table>
        </el-card>

        <el-card class="bid-ask-card">
          <div slot="header">
            <span>卖五档</span>
          </div>
          <el-table
            :data="askData"
            style="width: 100%;"
            :show-header="false"
            v-loading="loading"
          >
            <el-table-column prop="price" width="120">
              <template slot-scope="scope">
                <span class="price-red">{{ scope.row.price }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="volume" label="数量"></el-table-column>
          </el-table>
        </el-card>
      </div>
    </el-card>
  </div>
</template>

<script>
import { stockApi } from '../utils/api'
import './StockAuction.css'

export default {
  name: 'StockAuction',
  data() {
    return {
      stockCode: '',
      auctionData: [],
      bidData: [],
      askData: [],
      loading: false
    }
  },
  methods: {
    handleQuery() {
      if (!this.stockCode) {
        this.$message.warning('请输入股票代码')
        return
      }
      this.loadAuctionData()
      this.loadBidAskData()
    },
    refreshData() {
      if (this.stockCode) {
        this.handleQuery()
      } else {
        this.$message.warning('请先输入股票代码')
      }
    },
    async loadAuctionData() {
      this.loading = true
      try {
        const response = await stockApi.getAuctionData(this.stockCode)
        this.auctionData = response.data || []
      } catch (error) {
        this.$message.error('加载集合竞价数据失败: ' + error.message)
      }
    },
    async loadBidAskData() {
      try {
        const response = await stockApi.getBidAskData(this.stockCode)
        const dataList = response.data || []
        if (dataList.length > 0) {
          const data = dataList[0]
          this.bidData = this.formatBidAskData(data, 'bid')
          this.askData = this.formatBidAskData(data, 'ask')
        }
      } catch (error) {
        this.$message.error('加载买卖五档数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    formatBidAskData(data, type) {
      const result = []
      for (let i = 1; i <= 5; i++) {
        const priceKey = type === 'bid' ? `买${i}价` : `卖${i}价`
        const volumeKey = type === 'bid' ? `买${i}量` : `卖${i}量`
        if (data[priceKey] !== undefined && data[volumeKey] !== undefined) {
          result.push({
            price: data[priceKey],
            volume: data[volumeKey]
          })
        }
      }
      return result
    },
    getPriceColor(price, changePercent) {
      if (changePercent !== undefined) {
        if (changePercent > 0) return '#f56c6c'
        if (changePercent < 0) return '#67c23a'
      }
      return '#303133'
    }
  }
}
</script>
