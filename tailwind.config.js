/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/views/**/*.ejs",
    "./src/public/js/**/*.js"
  ],
  darkMode: 'class', // Enable dark mode via class strategy
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'monospace'],
      },
      colors: {
        // Trust & Security Palette
        brand: {
          DEFAULT: '#0F172A', // slate-900
          light: '#1E293B',   // slate-800
        },
        primary: {
          DEFAULT: '#2563EB', // blue-600
          hover: '#1D4ED8',   // blue-700
          light: '#3B82F6',   // blue-500
        },
        success: {
          DEFAULT: '#059669', // emerald-600
          light: '#10B981',   // emerald-500
        },
        alert: {
          DEFAULT: '#DC2626', // rose-600
          light: '#EF4444',   // rose-500
        },
      },
    },
  },
  plugins: [
    require('@tailwindcss/forms'),
    require('@tailwindcss/typography'),
  ],
}