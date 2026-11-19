import { useEffect, useState } from 'react';

/** Depois de alguns segundos esperando a API, avisa que o servidor gratuito pode estar acordando. */
export function useDemorando(ativo, segundos = 5) {
  const [demorando, setDemorando] = useState(false);

  useEffect(() => {
    if (!ativo) {
      setDemorando(false);
      return undefined;
    }
    const timer = setTimeout(() => setDemorando(true), segundos * 1000);
    return () => clearTimeout(timer);
  }, [ativo, segundos]);

  return demorando;
}

export function AvisoServidorIniciando() {
  return (
    <p className="aviso-servidor">
      O servidor gratuito estava em repouso e está iniciando. Isso pode levar cerca de 1 minuto.
    </p>
  );
}

export default function Carregando() {
  const demorando = useDemorando(true);
  return (
    <div className="carregando">
      <p>Carregando...</p>
      {demorando && <AvisoServidorIniciando />}
    </div>
  );
}
