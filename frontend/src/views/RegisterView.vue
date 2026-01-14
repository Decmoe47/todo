<template>
  <div style="max-width: 300px; margin: auto; padding: 20px">
    <div>
      <h1 style="text-align: center; margin: 20px 0">Register</h1>
    </div>
    <el-form
      ref="registerForm"
      :model="form"
      :rules="rules"
      style="max-width: 300px"
      @submit.prevent="handleSubmit"
      :inline-message="true"
    >
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
            <el-button @click="getVerificationCode" :disabled="isCodeSendingOrSent" style="height: 40px; font-size: 14px; font-weight: bold;">
              {{ 
                verificationCodeSentStatus === VerificationCodeSentStatusEnums.SENDING ? 
                  'Sending...' : verificationCodeSentStatus === VerificationCodeSentStatusEnums.SENT ? 
                    `Retry after ${timer}` : 'Get Code' 
              }}
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
import { VerificationCodeSentStatusEnums } from '@/constants/enums'
import { useUserStore } from '@/stores/user.ts'
import type { RegisterForm } from '@/types/user.ts'
import { ElMessage } from 'element-plus'
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const store = useUserStore()

const timer = ref(0)
const verificationCodeSentStatus = ref(VerificationCodeSentStatusEnums.NOT_SEND)
const isCodeSendingOrSent = computed(
  () => verificationCodeSentStatus.value === VerificationCodeSentStatusEnums.SENDING || verificationCodeSentStatus.value === VerificationCodeSentStatusEnums.SENT
)
const form = reactive<RegisterForm>({
  name: '',
  email: '',
  password: '',
  verificationCode: '',
})

const rules = {
  name: [{ required: true, message: 'Please enter your name', trigger: 'blur' }],
  email: [
    { required: true, message: 'Please enter your email address', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'Please enter your password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9!@#$%^&*()_+={}\[\]:;"'<>,.?~`-]+$/,
      message: 'Password can only contain letters, numbers, and special characters !@#$%^&*()_+={}[]:;"\'<>,.?~`-',
      trigger: 'blur',
    },
  ],
  verificationCode: [
    { required: true, message: 'Please enter the verification code', trigger: 'blur' },
    { len: 4, message: 'Verification code must be 4 digits', trigger: 'blur' },
  ],
}

const handleSubmit = async () => {
  await store.register(form)
  ElMessage.success('Register successfully')
  await router.replace('/login')
}

// 在组件挂载时恢复倒计时状态
onMounted(() => {
  const expires = localStorage.getItem('verifyCodeExpires')
  if (expires) {
    const remaining = Math.floor((parseInt(expires) - Date.now()) / 1000)
    if (remaining > 0) {
      timer.value = remaining
      verificationCodeSentStatus.value = VerificationCodeSentStatusEnums.SENT

      countdown()
    } else {
      localStorage.removeItem('verifyCodeExpires')
    }
  }
})

const getVerificationCode = async () => {
  if (!form.email) {
    ElMessage.error('Please enter the email')
    return
  }
  try {
    verificationCodeSentStatus.value = VerificationCodeSentStatusEnums.SENDING
    timer.value = 60
    await store.sendVerificationCode(form.email)
    verificationCodeSentStatus.value = VerificationCodeSentStatusEnums.SENT
    // 保存倒计时结束的时间戳（当前时间 + 60秒）
    localStorage.setItem('verificationCodeExpires', String(Date.now() + 60000))
    ElMessage.success('Send verification code successfully')

    countdown()
  } catch (e) {
    verificationCodeSentStatus.value = VerificationCodeSentStatusEnums.NOT_SEND
    console.error(e)
    ElMessage.error('Failed to send verification code. Please try again later.')
  }
}

// 再发送验证码倒计时
function countdown() {
  const interval = setInterval(() => {
    timer.value--
    if (timer.value <= 0) {
      clearInterval(interval)
      verificationCodeSentStatus.value = VerificationCodeSentStatusEnums.NOT_SEND
      localStorage.removeItem('verificationCodeExpires')
    }
  }, 1000)
}
</script>

<style scoped>
:deep(.el-form-item) {
  margin-bottom: 10px;
}
</style>
