import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import CreateQuotationPage from '../features/commercial/pages/CreateQuotationPage'
import CustomersPage from '../features/commercial/pages/CustomersPage'
import OrderDetailPage from '../features/commercial/pages/OrderDetailPage'
import OrdersPage from '../features/commercial/pages/OrdersPage'
import QuotationDetailPage from '../features/commercial/pages/QuotationDetailPage'
import QuotationsPage from '../features/commercial/pages/QuotationsPage'
import FinancePage from '../features/finance/pages/FinancePage'
import HomePage from '../features/home/pages/HomePage'
import IntelligencePage from '../features/intelligence/pages/IntelligencePage'
import InventoryDetailPage from '../features/inventory/pages/InventoryDetailPage'
import InventoryPage from '../features/inventory/pages/InventoryPage'
import PlotterJobDetailPage from '../features/plotter/pages/PlotterJobDetailPage'
import PlotterJobsPage from '../features/plotter/pages/PlotterJobsPage'
import ProductionOrderDetailPage from '../features/production/pages/ProductionOrderDetailPage'
import ProductionOrdersPage from '../features/production/pages/ProductionOrdersPage'
import MainLayout from '../layout/MainLayout'

function AppRouter() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
          <Route index element={<Navigate to="/home" replace />} />
          <Route path="home" element={<HomePage />} />
          <Route path="commercial/new" element={<CreateQuotationPage />} />
          <Route path="commercial/customers" element={<CustomersPage />} />
          <Route
            path="commercial/orders/:orderId"
            element={<OrderDetailPage />}
          />
          <Route path="commercial/orders" element={<OrdersPage />} />
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
            path="inventory/:inventoryItemId"
            element={<InventoryDetailPage />}
          />
          <Route path="inventory" element={<InventoryPage />} />
          <Route
            path="plotter/jobs/:plotterJobId"
            element={<PlotterJobDetailPage />}
          />
          <Route path="plotter" element={<PlotterJobsPage />} />
          <Route path="finance" element={<FinancePage />} />
          <Route path="intelligence" element={<IntelligencePage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}

export default AppRouter
