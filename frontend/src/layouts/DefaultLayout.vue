<template>
  <el-container style="height: 100vh">
    <el-aside width="200px">
      <SidebarComp />
    </el-aside>
    <el-container>
      <el-main>
        <router-view v-slot="{ Component }">
          <component :is="Component" />
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import SidebarComp from '@/components/SidebarComp.vue'
</script>
