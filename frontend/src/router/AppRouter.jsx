import { Typography } from '@mui/material'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import CreateQuotationPage from '../features/commercial/pages/CreateQuotationPage'
import CustomersPage from '../features/commercial/pages/CustomersPage'
import QuotationDetailPage from '../features/commercial/pages/QuotationDetailPage'
import QuotationsPage from '../features/commercial/pages/QuotationsPage'
import IntelligencePage from '../features/intelligence/pages/IntelligencePage'
import ProductionOrderDetailPage from '../features/production/pages/ProductionOrderDetailPage'
import ProductionOrdersPage from '../features/production/pages/ProductionOrdersPage'
import MainLayout from '../layout/MainLayout'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/commercial" replace />} />
          <Route path="commercial/new" element={<CreateQuotationPage />} />
          <Route path="commercial/customers" element={<CustomersPage />} />
          <Route
            path="commercial/quotations/:quotationId"
            element={<QuotationDetailPage />}
          />
          <Route path="commercial" element={<QuotationsPage />} />
          <Route
            path="production/orders/:productionOrderId"
            element={<ProductionOrderDetailPage />}
          />
          <Route path="production" element={<ProductionOrdersPage />} />
          <Route
            path="inventory"
            element={<Typography>Inventory Page</Typography>}
          />
          <Route
            path="finance"
            element={<Typography>Finance Page</Typography>}
          />
          <Route path="intelligence" element={<IntelligencePage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
