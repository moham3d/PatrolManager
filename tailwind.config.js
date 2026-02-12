/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/views/**/*.ejs",
    "./src/public/js/**/*.js"
  ],
  theme: {
    extend: {
      colors: {
        // Matching Android Material3 Theme (approximate)
        primary: '#3B82F6', // Blue-600
        secondary: '#10B981', // Green-500
        danger: '#EF4444', // Red-500
        warning: '#F59E0B', // Amber-500
        dark: '#1F2937', // Gray-800
        light: '#F3F4F6', // Gray-100
      },
    },
  },
  plugins: [],
}