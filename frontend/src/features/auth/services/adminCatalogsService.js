import httpClient from '../../../services/httpClient'

export async function getAdminCatalogs() {
  const response = await httpClient.get('/admin/catalogs')
  return response.data
}

export async function createAdminCatalogEntry(catalogKind, name) {
  const response = await httpClient.post(`/admin/catalogs/${catalogKind}`, { name })
  return response.data
}

export async function activateAdminCatalogEntry(catalogKind, catalogEntryId) {
  const response = await httpClient.patch(
    `/admin/catalogs/${catalogKind}/${catalogEntryId}/activate`
  )
  return response.data
}

export async function deactivateAdminCatalogEntry(catalogKind, catalogEntryId) {
  const response = await httpClient.patch(
    `/admin/catalogs/${catalogKind}/${catalogEntryId}/deactivate`
  )
  return response.data
}
