import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from '../features/auth/AuthProvider'
import AdminOnlyPage from '../features/auth/components/AdminOnlyPage'
import ProtectedRoute from '../features/auth/components/ProtectedRoute'
import PublicLoginRoute from '../features/auth/components/PublicLoginRoute'
import AdminUsersPage from '../features/auth/pages/AdminUsersPage'
import CreateQuotationPage from '../features/commercial/pages/CreateQuotationPage'
import CustomersPage from '../features/commercial/pages/CustomersPage'
import OrderDetailPage from '../features/commercial/pages/OrderDetailPage'
import OrdersPage from '../features/commercial/pages/OrdersPage'
import QuotationDetailPage from '../features/commercial/pages/QuotationDetailPage'
import QuotationsPage from '../features/commercial/pages/QuotationsPage'
import SellersPage from '../features/commercial/pages/SellersPage'
import FinancePage from '../features/finance/pages/FinancePage'
import HomePage from '../features/home/pages/HomePage'
import IntelligencePage from '../features/intelligence/pages/IntelligencePage'
import InventoryDetailPage from '../features/inventory/pages/InventoryDetailPage'
import InventoryPage from '../features/inventory/pages/InventoryPage'
import PlotterJobDetailPage from '../features/plotter/pages/PlotterJobDetailPage'
import PlotterJobsPage from '../features/plotter/pages/PlotterJobsPage'
import OperatorsPage from '../features/production/pages/OperatorsPage'
import ProductionOrderDetailPage from '../features/production/pages/ProductionOrderDetailPage'
import ProductionOrdersPage from '../features/production/pages/ProductionOrdersPage'
import MainLayout from '../layout/MainLayout'

function AppRouter() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<PublicLoginRoute />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/home" replace />} />
            <Route path="home" element={<HomePage />} />
            <Route path="commercial/new" element={<CreateQuotationPage />} />
            <Route path="commercial/customers" element={<CustomersPage />} />
            <Route path="commercial/sellers" element={<SellersPage />} />
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
            <Route path="production/operators" element={<OperatorsPage />} />
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
            <Route
              path="finance"
              element={
                <AdminOnlyPage>
                  <FinancePage />
                </AdminOnlyPage>
              }
            />
            <Route
              path="admin/users"
              element={
                <AdminOnlyPage>
                  <AdminUsersPage />
                </AdminOnlyPage>
              }
            />
            <Route
              path="intelligence"
              element={
                <AdminOnlyPage>
                  <IntelligencePage />
                </AdminOnlyPage>
              }
            />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}

export default AppRouter
