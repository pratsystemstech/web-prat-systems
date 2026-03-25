import Sitio from '@/components/Sitio';

export default function Page() {
  return <Sitio />;
}export const metadata = {
  title: "PRAT SYSTEMS",
  description: "Automatización y sistemas digitales",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="es">
      <body>{children}</body>
    </html>
  );
}body {
  margin: 0;
  font-family: Arial, sans-serif;
  background: #f9f9f9;
}

h1, h2, h3 {
  color: #111;
}

button {
  cursor: pointer;
}
