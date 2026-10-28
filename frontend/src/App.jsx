import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from './auth/AuthContext';
import RotaPrivada from './components/RotaPrivada';
import { PAGINA_INICIAL } from './formatar';
import Login from './pages/Login';
import CadastroAluno from './pages/CadastroAluno';
import CadastroEmpresa from './pages/CadastroEmpresa';
import AlunoExtrato from './pages/aluno/AlunoExtrato';
import AlunoVantagens from './pages/aluno/AlunoVantagens';
import AlunoPerfil from './pages/aluno/AlunoPerfil';
import ProfessorEnviar from './pages/professor/ProfessorEnviar';
import ProfessorExtrato from './pages/professor/ProfessorExtrato';
import ProfessorPerfil from './pages/professor/ProfessorPerfil';
import EmpresaVantagens from './pages/empresa/EmpresaVantagens';
import EmpresaResgates from './pages/empresa/EmpresaResgates';
import EmpresaPerfil from './pages/empresa/EmpresaPerfil';

function Inicio() {
  const { usuario, carregando } = useAuth();
  if (carregando) return <p className="carregando">Carregando...</p>;
  return <Navigate to={usuario ? PAGINA_INICIAL[usuario.perfil] : '/login'} replace />;
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Inicio />} />
      <Route path="/login" element={<Login />} />
      <Route path="/cadastro/aluno" element={<CadastroAluno />} />
      <Route path="/cadastro/empresa" element={<CadastroEmpresa />} />

      <Route element={<RotaPrivada perfil="ALUNO" />}>
        <Route path="/aluno/extrato" element={<AlunoExtrato />} />
        <Route path="/aluno/vantagens" element={<AlunoVantagens />} />
        <Route path="/aluno/perfil" element={<AlunoPerfil />} />
      </Route>

      <Route element={<RotaPrivada perfil="PROFESSOR" />}>
        <Route path="/professor/enviar" element={<ProfessorEnviar />} />
        <Route path="/professor/extrato" element={<ProfessorExtrato />} />
        <Route path="/professor/perfil" element={<ProfessorPerfil />} />
      </Route>

      <Route element={<RotaPrivada perfil="EMPRESA" />}>
        <Route path="/empresa/vantagens" element={<EmpresaVantagens />} />
        <Route path="/empresa/resgates" element={<EmpresaResgates />} />
        <Route path="/empresa/perfil" element={<EmpresaPerfil />} />
      </Route>

      <Route path="*" element={<Inicio />} />
    </Routes>
  );
}
