import { formatarMoedas } from '../formatar';

export default function Saldo({ valor, rotulo = 'Saldo disponível', children }) {
  return (
    <section className="saldo">
      <div>
        <span className="saldo-rotulo">{rotulo}</span>
        <span className="saldo-valor">{valor == null ? '—' : formatarMoedas(valor)}</span>
      </div>
      {children}
    </section>
  );
}
