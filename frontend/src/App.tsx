import { BrowserRouter, Route, Routes } from 'react-router-dom';

import { PrivateRoute } from './components/auth/PrivateRoute';
import { MainLayout } from './components/layout/MainLayout';
import { AuthProvider } from './context/AuthContext';
import { LoginPage } from './pages/LoginPage';
import { RegisterPage } from './pages/RegisterPage';
import { TasksPage } from './pages/TasksPage';

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/"
            element={
              <PrivateRoute>
                <MainLayout />
              </PrivateRoute>
            }
          >
            <Route
              index
              element={
                <div className="flex h-full items-center justify-center">
                  <div className="text-center">
                    <h1 className="text-4xl font-bold text-gray-900">Welcome to TODO App</h1>
                    <p className="mt-4 text-lg text-gray-600">
                      Your full-stack task management solution
                    </p>
                  </div>
                </div>
              }
            />
            <Route path="/tasks" element={<TasksPage />} />
          </Route>
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
