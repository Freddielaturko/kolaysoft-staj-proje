import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Modal({ title, eyebrow, onClose, children, wide = false }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className={wide ? 'modal wide-modal' : 'modal'} role="dialog" aria-modal="true">
        <div className="modal-head">
          <div>
            {eyebrow && <p className="eyebrow blue">{eyebrow}</p>}
            <h3>{title}</h3>
          </div>
          <button className="modal-close" onClick={onClose} aria-label="Kapat">
            <X size={18} />
          </button>
        </div>
        <div className="modal-body">{children}</div>
      </div>
    </div>
  )
}
