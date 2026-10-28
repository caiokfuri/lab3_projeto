import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { PAGINA_INICIAL } from '../formatar';
import Layout from './Layout';

export default function RotaPrivada({ perfil }) {
  const { usuario, carregando } = useAuth();
  const local = useLocation();

  if (carregando) return <p className="carregando">Carregando...</p>;
  if (!usuario) return <Navigate to="/login" replace state={{ de: local.pathname }} />;
  if (perfil && usuario.perfil !== perfil) return <Navigate to={PAGINA_INICIAL[usuario.perfil]} replace />;

  return (
    <Layout>
      <Outlet />
    </Layout>
  );
}
