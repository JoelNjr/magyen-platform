import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from '../features/auth/AuthProvider'
import AdminOnlyPage from '../features/auth/components/AdminOnlyPage'
import ProtectedRoute from '../features/auth/components/ProtectedRoute'
import PublicLoginRoute from '../features/auth/components/PublicLoginRoute'
import { useAuth } from '../features/auth/AuthContext'
import { resolveDefaultAuthenticatedPath } from '../features/auth/presentation/authPresentation'
import AdminCatalogsPage from '../features/auth/pages/AdminCatalogsPage'
import AdminUsersPage from '../features/auth/pages/AdminUsersPage'
import CreateQuotationPage from '../features/commercial/pages/CreateQuotationPage'
import CustomersPage from '../features/commercial/pages/CustomersPage'
import OrderDetailPage from '../features/commercial/pages/OrderDetailPage'
import OrderProfitabilityDetailPage from '../features/commercial/pages/OrderProfitabilityDetailPage'
import OrderProfitabilityPage from '../features/commercial/pages/OrderProfitabilityPage'
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
import PlotterProfitabilityPage from '../features/plotter/pages/PlotterProfitabilityPage'
import ProductionOrderDetailPage from '../features/production/pages/ProductionOrderDetailPage'
import ProductionOrdersPage from '../features/production/pages/ProductionOrdersPage'
import MainLayout from '../layout/MainLayout'

function DefaultAuthenticatedRedirect() {
  const { identity } = useAuth()
  return <Navigate to={resolveDefaultAuthenticatedPath(identity)} replace />
}

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
            <Route index element={<DefaultAuthenticatedRedirect />} />
            <Route
              path="home"
              element={
                <AdminOnlyPage redirectTo="/commercial">
                  <HomePage />
                </AdminOnlyPage>
              }
            />
            <Route path="commercial/new" element={<CreateQuotationPage />} />
            <Route path="commercial/customers" element={<CustomersPage />} />
            <Route path="commercial/sellers" element={<SellersPage />} />
            <Route
              path="commercial/orders/profitability"
              element={<OrderProfitabilityPage />}
            />
            <Route
              path="commercial/orders/:orderId/profitability"
              element={<OrderProfitabilityDetailPage />}
            />
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
              path="plotter/profitability"
              element={<PlotterProfitabilityPage />}
            />
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
              path="admin/catalogs"
              element={
                <AdminOnlyPage>
                  <AdminCatalogsPage />
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
