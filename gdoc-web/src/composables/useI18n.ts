import { ref } from 'vue'
import zhCN from '@/i18n/zh-CN'
import enUS from '@/i18n/en-US'

type Messages = typeof zhCN

const messages: Record<string, Messages> = {
  'zh-CN': zhCN,
  'en-US': enUS,
}

const currentLocale = ref<string>('zh-CN')

export function useI18n() {
  const t = ref(zhCN)

  function setLocale(locale: string) {
    currentLocale.value = locale
    t.value = messages[locale] || zhCN
  }

  function translate(key: string): string {
    const keys = key.split('.')
    let result: any = t.value
    for (const k of keys) {
      result = result?.[k]
      if (result === undefined) return key
    }
    return String(result)
  }

  return { t, currentLocale, setLocale, translate }
}