import { useSearchParams, useNavigate } from 'react-router-dom'
import { Plus } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { usePortfolio } from '../context/PortfolioContext'
import ProjectTable from '../components/ProjectTable'
import ProjectForm from '../components/ProjectForm'
import Modal from '../components/Modal'

export default function Projects() {
  const { user } = useAuth()
  const { projects, loading, error, reload } = usePortfolio()
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const formOpen = params.get('yeni') === '1' && user.rol === 'ADMIN'

  if (loading) return <div className="loading">Projeler yükleniyor...</div>

  return (
    <>
      {error && <div className="page-error">{error}</div>}
      <ProjectTable
        projects={projects}
        title="Tüm projeler"
        action={user.rol === 'ADMIN' && (
          <button className="primary-button" onClick={() => setParams({ yeni: '1' })}>
            <Plus size={16} /> Proje ekle
          </button>
        )}
      />
      {formOpen && (
        <Modal title="Yeni proje" eyebrow="PORTFÖY" onClose={() => setParams({})} wide>
          <ProjectForm
            onCancel={() => setParams({})}
            onSaved={async (saved) => {
              await reload()
              navigate(`/projeler/${saved.id}`)
            }}
          />
        </Modal>
      )}
    </>
  )
}
