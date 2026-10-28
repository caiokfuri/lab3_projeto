import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, definirAoExpirarSessao, token } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(null);
  const [carregando, setCarregando] = useState(() => Boolean(token.ler()));

  const encerrarSessaoLocal = useCallback(() => {
    token.limpar();
    setUsuario(null);
  }, []);

  // Restaura a sessão salva ao recarregar a página.
  useEffect(() => {
    definirAoExpirarSessao(encerrarSessaoLocal);
    if (!token.ler()) return;
    api.get('/api/auth/me')
      .then(setUsuario)
      .catch(encerrarSessaoLocal)
      .finally(() => setCarregando(false));
  }, [encerrarSessaoLocal]);

  const entrar = useCallback(async (login, senha) => {
    const resposta = await api.post('/api/auth/login', { login, senha });
    token.salvar(resposta.token);
    setUsuario(resposta.usuario);
    return resposta.usuario;
  }, []);

  const sair = useCallback(async () => {
    try {
      await api.post('/api/auth/logout');
    } catch {
      // a sessão local é encerrada mesmo se o servidor estiver fora do ar
    }
    encerrarSessaoLocal();
  }, [encerrarSessaoLocal]);

  const valor = useMemo(
    () => ({ usuario, carregando, entrar, sair, encerrarSessaoLocal, atualizarUsuario: setUsuario }),
    [usuario, carregando, entrar, sair, encerrarSessaoLocal],
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
