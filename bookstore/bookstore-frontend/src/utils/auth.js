export const setToken = (token) => {
  localStorage.setItem('token', token)
}

export const getToken = () => {
  return localStorage.getItem('token')
}

export const removeToken = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('username')
}

export const setUserInfo = (username) => {
  localStorage.setItem('username', username)
}

export const getUsername = () => {
  return localStorage.getItem('username')
}

export const isAuthenticated = () => {
  return !!getToken()
}