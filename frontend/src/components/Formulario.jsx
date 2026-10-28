import { useCallback, useEffect, useRef, useState } from 'react';

export function useFormulario(inicial) {
  const [valores, setValores] = useState(inicial);
  const [erros, setErros] = useState({});

  const alterar = useCallback((evento) => {
    const { name, value } = evento.target;
    setValores((v) => ({ ...v, [name]: value }));
    setErros((e) => (e[name] ? { ...e, [name]: undefined } : e));
  }, []);

  return { valores, setValores, erros, setErros, alterar };
}

export function useMensagem() {
  const [mensagem, setMensagem] = useState(null);
  const timer = useRef();

  const mostrar = useCallback((texto, tipo = 'sucesso') => {
    setMensagem({ texto, tipo });
    clearTimeout(timer.current);
    timer.current = setTimeout(() => setMensagem(null), 6000);
  }, []);

  useEffect(() => () => clearTimeout(timer.current), []);

  return [mensagem, mostrar];
}

export function Mensagem({ mensagem }) {
  if (!mensagem) return null;
  return <div className={`mensagem ${mensagem.tipo}`} role="status">{mensagem.texto}</div>;
}

/** Rótulo + controle + erro. Sem children, renderiza um <input> com as props recebidas. */
export function Campo({ rotulo, name, erro, dica, largo, children, ...props }) {
  return (
    <div className={`campo${largo ? ' largo' : ''}${erro ? ' invalido' : ''}`}>
      <label htmlFor={name}>{rotulo}</label>
      {children ?? <input id={name} name={name} {...props} />}
      {erro ? <span className="erro-campo">{erro}</span> : dica && <span className="dica">{dica}</span>}
    </div>
  );
}

export function Modal({ titulo, children, aoFechar }) {
  useEffect(() => {
    const aoTeclar = (e) => e.key === 'Escape' && aoFechar();
    window.addEventListener('keydown', aoTeclar);
    return () => window.removeEventListener('keydown', aoTeclar);
  }, [aoFechar]);

  return (
    <div className="modal-fundo" onClick={aoFechar}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={titulo} onClick={(e) => e.stopPropagation()}>
        <h2>{titulo}</h2>
        {children}
      </div>
    </div>
  );
}
