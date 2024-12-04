import axios, { AxiosError } from 'axios'
import { ElMessage } from 'element-plus'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  timeout: 5000,
})

axiosInstance.interceptors.response.use(
  async (response) => {
    if (response.data.code === 0) {
      return response.data.data
    } else {
      if (response.data.code !== 10003) {
        ElMessage.error(response.data.message)
        console.error(JSON.stringify(response.data))
      }
      return Promise.reject(new Error(`${response.data.code}: ${response.data.message}`))
    }
  },
  async (error: AxiosError) => {
    let msg = error.message
    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          msg = 'Invalid request body'
          break
        case 401:
          msg = 'Unauthorized, please login'
          break
        case 403:
          msg = 'Forbidden'
          break
        case 404:
          msg = 'Not Found'
          break
        case 500:
          msg = 'Internal Server Error'
          break
        default:
          msg = `Connection error ${status}`
      }
    } else if (error.code === 'ECONNABORTED' && error.message.indexOf('timeout') !== -1) {
      msg = 'Timeout'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  },
)

export default axiosInstance
