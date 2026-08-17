import { useEffect, useState } from 'react'
import { loadCommercialCatalogs } from '../presentation/commercialCatalogs'

const EMPTY_CATALOGS = {
  garmentTypes: [],
  collarTypes: [],
  sleeveTypes: [],
  cuffOptions: [],
  fabrics: [],
}

export function useCommercialCatalogs() {
  const [catalogs, setCatalogs] = useState(EMPTY_CATALOGS)
  const [loading, setLoading] = useState(true)
  const [failed, setFailed] = useState(false)

  useEffect(() => {
    let cancelled = false
    setLoading(true)
    setFailed(false)

    loadCommercialCatalogs()
      .then((data) => {
        if (cancelled) {
          return
        }
        setCatalogs({
          garmentTypes: Array.isArray(data?.garmentTypes) ? data.garmentTypes : [],
          collarTypes: Array.isArray(data?.collarTypes) ? data.collarTypes : [],
          sleeveTypes: Array.isArray(data?.sleeveTypes) ? data.sleeveTypes : [],
          cuffOptions: Array.isArray(data?.cuffOptions) ? data.cuffOptions : [],
          fabrics: Array.isArray(data?.fabrics) ? data.fabrics : [],
        })
        setLoading(false)
      })
      .catch(() => {
        if (cancelled) {
          return
        }
        setCatalogs(EMPTY_CATALOGS)
        setFailed(true)
        setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  return { catalogs, loading, failed }
}

export function toSelectOptions(options) {
  return (options ?? []).map((option) => ({
    value: String(option.value),
    label: option.label,
  }))
}

export function withCurrentOption(options, currentValue) {
  if (currentValue == null || currentValue === '') {
    return options
  }
  const current = String(currentValue)
  if (options.some((option) => String(option.value) === current)) {
    return options
  }
  return [{ value: current, label: current }, ...options]
}
