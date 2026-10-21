export default function Badge({ children, tone = 'normal' }) {
  return <span className={`badge badge-${tone}`}>{children}</span>
}
