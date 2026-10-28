import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento (`npm run dev`, porta 5173) as chamadas à API e às fotos são repassadas ao Spring na 8080.
// Em produção (`npm run build`) o Spring serve a pasta dist/ e tudo fica na mesma origem.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
      '/uploads': 'http://localhost:8080',
    },
  },
});
