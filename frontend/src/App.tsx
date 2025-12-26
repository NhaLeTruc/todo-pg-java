import { BrowserRouter, Route, Routes } from 'react-router-dom';

import { MainLayout } from './components/layout/MainLayout';
import { TasksPage } from './pages/TasksPage';

export function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainLayout />}>
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
    </BrowserRouter>
  );
}
