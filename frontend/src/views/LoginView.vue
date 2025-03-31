<template>
  <div style="max-width: 300px; margin: auto; padding: 20px">
    <div>
      <h1 style="text-align: center; margin: 20px 0">Login</h1>
    </div>
    <el-form :model="form" :rules="rules" ref="loginForm" style="max-width: 300px" @submit.prevent="handleSubmit">
      <el-form-item prop="email">
        <el-input v-model="form.email" prefix-icon="user" />
      </el-form-item>

      <el-form-item prop="password">
        <el-input type="password" v-model="form.password" prefix-icon="lock" />
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          style="height: 40px; font-size: 18px; font-weight: bold; margin: 10px auto; width: 100%"
          native-type="submit"
          :disabled="!form.email || !form.password"
        >
          Login
        </el-button>
      </el-form-item>

      <div style="margin-top: 16px; text-align: center">or <router-link to="/register">register now</router-link></div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const store = useUserStore()

const form = ref({
  email: '',
  password: '',
})

const rules = {
  email: [{ required: true, message: 'Please enter your email address', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }],
}

const handleSubmit = async () => {
  const loginForm = form.value
  if (!loginForm.email || !loginForm.password) {
    return
  }

  await store.login(loginForm)
  ElMessage.success('Login successfully')
  await router.replace('/')
}
</script>
