export const localSave = (key: string, value: any) => {
  localStorage.setItem(key, value)
}

export const localRead = (key: string) => {
  return localStorage.getItem(key) || ''
}

export const localClear = () => {
  localStorage.clear()
}

export const sessionRead = (key: string) => {
  return sessionStorage.getItem(key)
}

export const sessionSave = (key: string, value: any) => {
  sessionStorage.setItem(key, value)
}

export const sessionClear = () => {
  sessionStorage.clear()
}
