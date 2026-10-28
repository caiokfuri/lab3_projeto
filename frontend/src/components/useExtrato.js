import { useCallback, useEffect, useState } from 'react';
import { api } from '../api';

/** Saldo e transações do aluno/professor logado. */
export default function useExtrato() {
  const [extrato, setExtrato] = useState(null);
  const [erro, setErro] = useState(null);

  const recarregar = useCallback(async () => {
    try {
      setExtrato(await api.get('/api/transacoes/extrato'));
      setErro(null);
    } catch (e) {
      setErro(e.message);
    }
  }, []);

  useEffect(() => { recarregar(); }, [recarregar]);

  return { extrato, erro, recarregar };
}
