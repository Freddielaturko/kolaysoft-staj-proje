import { Routes, Route } from 'react-router-dom'

// TODO: Auth sonrasi burasi Login, PM Dashboard, CTO Dashboard, Admin
// sayfalarina yonlendirilecek. Simdilik iskelet.
function App() {
  return (
    <Routes>
      <Route path="/" element={<div>Proje Durum Sistemi - iskelet calisiyor</div>} />
    </Routes>
  )
}

export default App
