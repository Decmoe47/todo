<template>
  <div style="max-width: 300px; margin: auto; padding: 20px">
    <div>
      <h1 style="text-align: center; margin: 20px 0">Register</h1>
    </div>
    <el-form ref="registerForm" :model="form" :rules="rules" style="max-width: 300px" @submit.prevent="handleSubmit">
      <el-form-item prop="name">
        <el-input v-model="form.name" placeholder="name" prefix-icon="user" />
      </el-form-item>

      <el-form-item prop="email">
        <el-input v-model="form.email" placeholder="email" prefix-icon="user" />
      </el-form-item>

      <el-form-item prop="password">
        <el-input v-model="form.password" placeholder="password" type="password" prefix-icon="lock" />
      </el-form-item>

      <el-form-item prop="verificationCode">
        <el-input v-model="form.verificationCode" placeholder="verification code" maxlength="4">
          <template #append>
            <el-button @click="getVerificationCode" :disabled="verificationCodeSent">
              {{ verificationCodeSent ? `Retry after ${timer}` : 'Get Code' }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          style="height: 40px; font-size: 18px; font-weight: bold; margin: 10px auto; width: 100%"
          :disabled="!form.email || !form.password || !form.verificationCode"
        >
          Register
        </el-button>
      </el-form-item>

      <div style="margin-top: 10px; text-align: center">or <router-link to="/login">login now</router-link></div>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user.ts'
import type { RegisterForm } from '@/types/user.ts'

const router = useRouter()
const store = useUserStore()

const timer = ref(0)
const verificationCodeSent = ref(false)
const form = reactive<RegisterForm>({
  name: '',
  email: '',
  password: '',
  verificationCode: '',
})

const rules = {
  email: [
    { required: true, message: 'Please enter your email address', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' },
  ],
  password: [{ required: true, message: 'Please enter your password', trigger: 'blur' }],
  verificationCode: [
    { required: true, message: 'Please enter the verification code', trigger: 'blur' },
    { len: 4, message: 'Verification code must be 4 digits', trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  try {
    await store.register(form)
    ElMessage.success('Register successfully')
    await router.replace('/login')
  } catch (error) {
    console.error(error)
    ElMessage.error('Register failed! Please try again later.')
  }
}

const getVerificationCode = async () => {
  if (!form.email) {
    ElMessage.error('Please enter the email')
    return
  }
  try {
    verificationCodeSent.value = true
    await store.sendVerificationCode(form.email)
    ElMessage.success('Send verification code successfully')

    timer.value = 60
    const interval = setInterval(() => {
      timer.value--
      if (timer.value <= 0) {
        clearInterval(interval)
        verificationCodeSent.value = false
      }
    }, 1000)
  } catch (e) {
    console.error(e)
    ElMessage.error('Failed to send verification code. Please try again.')
  }
}
</script>
