import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Em desenvolvimento (`npm run dev`, porta 5173) as chamadas à API (inclusive fotos) são repassadas ao Spring na 8080.
// No build local o Spring serve a pasta dist/; na Vercel, VITE_API_URL aponta para a API na Render.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
});
