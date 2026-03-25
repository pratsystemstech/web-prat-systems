'use client';

import { useMemo, useState } from 'react';

type AuditAnswers = {
  employees: string;
  tools: string;
  repetitiveWork: string;
  leadTracking: string;
};

type ProposalAnswers = {
  company: string;
  sector: string;
  need: string;
  urgency: string;
};

const services = [
  {
    title: 'Automatización de procesos',
    text: 'Eliminamos tareas repetitivas y conectamos herramientas para que el trabajo fluya sin fricción.'
  },
  {
    title: 'Asistentes IA',
    text: 'Creamos asistentes guiados para atención, análisis, presupuestos y soporte operativo.'
  },
  {
    title: 'Captación digital',
    text: 'Diseñamos formularios, respuestas automáticas y seguimientos para convertir visitas en oportunidades.'
  },
  {
    title: 'Sistemas de control',
    text: 'Organizamos procesos, datos y seguimiento para que la empresa trabaje con más claridad.'
  }
];

const cases = [
  {
    title: 'Construcción',
    text: 'Seguimiento de obra, incidencias, partes, coordinación de equipos y control documental.'
  },
  {
    title: 'Empresas de servicios',
    text: 'Captación, propuestas, agenda, seguimiento comercial y automatización de respuestas.'
  },
  {
    title: 'Gestión administrativa',
    text: 'Procesos repetitivos, documentación, control interno y tareas manuales digitalizadas.'
  }
];

const processSteps = ['Análisis', 'Diseño del sistema', 'Implementación', 'Optimización'];

export default function Site() {
  const [mobileMenu, setMobileMenu] = useState(false);
  const [hoursWeek, setHoursWeek] = useState(10);
  const [costHour, setCostHour] = useState(20);
  const [people, setPeople] = useState(2);
  const [audit, setAudit] = useState<AuditAnswers>({
    employees: '1-5',
    tools: 'Básicas',
    repetitiveWork: 'Sí',
    leadTracking: 'Manual'
  });
  const [proposal, setProposal] = useState<ProposalAnswers>({
    company: '',
    sector: 'Servicios',
    need: 'Automatización de procesos',
    urgency: 'Media'
  });

  const savings = useMemo(() => {
    const monthlyHours = hoursWeek * 4.33 * people;
    const monthlyCost = monthlyHours * costHour;
    const estimatedSavings = monthlyCost * 0.45;
    const roiMonths = estimatedSavings > 0 ? Math.max(2, Math.round(1800 / estimatedSavings)) : 0;
    return { monthlyHours, monthlyCost, estimatedSavings, roiMonths };
  }, [hoursWeek, costHour, people]);

  const auditResult = useMemo(() => {
    let score = 0;
    if (audit.employees === '6-15' || audit.employees === '16+') score += 1;
    if (audit.tools !== 'Básicas') score += 1;
    if (audit.repetitiveWork === 'Sí') score += 2;
    if (audit.leadTracking === 'Manual') score += 2;
    if (audit.leadTracking === 'Mixto') score += 1;

    if (score >= 5) {
      return {
        level: 'Impacto alto',
        text: 'Tu empresa tiene margen claro para automatizar seguimiento, formularios, avisos y control interno.',
        next: 'Prioridad: captación + seguimiento + organización operativa.'
      };
    }
    if (score >= 3) {
      return {
        level: 'Impacto medio',
        text: 'Hay procesos que ya funcionan, pero todavía hay puntos manuales que te hacen perder tiempo.',
        next: 'Prioridad: centralizar información y automatizar tareas repetitivas.'
      };
    }
    return {
      level: 'Impacto inicial',
      text: 'Tu estructura puede mejorar con automatizaciones puntuales y una base de seguimiento más clara.',
      next: 'Prioridad: diagnóstico técnico y diseño de sistema simple.'
    };
  }, [audit]);

  const proposalText = useMemo(() => {
    const sectorText = proposal.sector.toLowerCase();
    const needText = proposal.need.toLowerCase();
    const urgencyText = proposal.urgency.toLowerCase();
    return `Proyecto recomendado para ${proposal.company || 'tu empresa'}: solución enfocada en ${needText} para el sector ${sectorText}. Planteamiento inicial: análisis, diseño del flujo, implementación y pruebas. Urgencia detectada: ${urgencyText}.`;
  }, [proposal]);

  return (
    <main>
      <header className="header">
        <div className="container nav-wrap">
          <a href="#top" className="brand">PRAT SYSTEMS</a>
          <nav className={`nav ${mobileMenu ? 'nav-open' : ''}`}>
            <a href="#servicios">Servicios</a>
            <a href="#demos">Demos</a>
            <a href="#casos">Casos de uso</a>
            <a href="#propuesta">Propuesta</a>
            <a href="#contacto">Contacto</a>
          </nav>
          <button className="menu-btn" onClick={() => setMobileMenu(!mobileMenu)} aria-label="Abrir menú">
            ☰
          </button>
        </div>
      </header>

      <section id="top" className="hero section">
        <div className="container hero-grid">
          <div>
            <span className="eyebrow">Automatización · IA · Sistemas digitales</span>
            <h1>Automatización e inteligencia artificial para empresas que quieren trabajar mejor</h1>
            <p className="lead">
              Diseñamos sistemas digitales que reducen tareas manuales, mejoran la gestión y optimizan la captación de clientes.
            </p>
            <div className="hero-actions">
              <a className="btn btn-primary" href="#demos">Ver demos</a>
              <a className="btn btn-secondary" href="#propuesta">Solicitar propuesta</a>
            </div>
            <div className="kpis">
              <div className="card stat"><strong>+45%</strong><span>Ahorro potencial en tareas repetitivas</span></div>
              <div className="card stat"><strong>4 fases</strong><span>Método claro de análisis a implementación</span></div>
              <div className="card stat"><strong>1 objetivo</strong><span>Que el sistema trabaje por la empresa</span></div>
            </div>
          </div>

          <div className="flow-box card">
            <div className="flow-step">Lead entra por formulario</div>
            <div className="flow-arrow">↓</div>
            <div className="flow-step">Sistema clasifica la solicitud</div>
            <div className="flow-arrow">↓</div>
            <div className="flow-step">Se envía respuesta y seguimiento</div>
            <div className="flow-arrow">↓</div>
            <div className="flow-step">La empresa gana control y tiempo</div>
          </div>
        </div>
      </section>

      <section className="section muted">
        <div className="container">
          <div className="section-title center">
            <span className="eyebrow">Problema real</span>
            <h2>Muchas empresas pierden tiempo en procesos manuales</h2>
            <p>No falla la intención. Falla el sistema. Cuando todo depende de llamadas, correos dispersos y seguimiento manual, se pierde tiempo y dinero.</p>
          </div>
          <div className="grid cols-3">
            {['Tareas repetitivas', 'Falta de seguimiento', 'Información desorganizada', 'Procesos lentos', 'Oportunidades perdidas', 'Falta de control'].map((item) => (
              <div key={item} className="card soft-card">{item}</div>
            ))}
          </div>
        </div>
      </section>

      <section id="servicios" className="section">
        <div className="container">
          <div className="section-title center">
            <span className="eyebrow">Servicios</span>
            <h2>Sistemas digitales adaptados a cada empresa</h2>
            <p>PRAT SYSTEMS crea soluciones que conectan herramientas, automatizan procesos y permiten trabajar con más claridad.</p>
          </div>
          <div className="grid cols-4">
            {services.map((service) => (
              <article key={service.title} className="card service-card">
                <h3>{service.title}</h3>
                <p>{service.text}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section id="demos" className="section muted">
        <div className="container">
          <div className="section-title center">
            <span className="eyebrow">Demos reales</span>
            <h2>Prueba sistemas antes de implementarlos</h2>
            <p>Aquí ya no hay teoría. Tienes cuatro bloques funcionales para mostrar valor y convertir visitas en oportunidades.</p>
          </div>

          <div className="demo-grid">
            <article className="card demo-card">
              <h3>Demo 1 · Auditoría rápida</h3>
              <p>Diagnóstico inicial del potencial de automatización.</p>
              <div className="form-grid compact">
                <label>
                  Tamaño
                  <select value={audit.employees} onChange={(e) => setAudit({ ...audit, employees: e.target.value })}>
                    <option>1-5</option>
                    <option>6-15</option>
                    <option>16+</option>
                  </select>
                </label>
                <label>
                  Herramientas
                  <select value={audit.tools} onChange={(e) => setAudit({ ...audit, tools: e.target.value })}>
                    <option>Básicas</option>
                    <option>Mezcladas</option>
                    <option>Integradas</option>
                  </select>
                </label>
                <label>
                  Trabajo repetitivo
                  <select value={audit.repetitiveWork} onChange={(e) => setAudit({ ...audit, repetitiveWork: e.target.value })}>
                    <option>Sí</option>
                    <option>No</option>
                  </select>
                </label>
                <label>
                  Seguimiento comercial
                  <select value={audit.leadTracking} onChange={(e) => setAudit({ ...audit, leadTracking: e.target.value })}>
                    <option>Manual</option>
                    <option>Mixto</option>
                    <option>Organizado</option>
                  </select>
                </label>
              </div>
              <div className="result-box">
                <strong>{auditResult.level}</strong>
                <p>{auditResult.text}</p>
                <span>{auditResult.next}</span>
              </div>
            </article>

            <article className="card demo-card">
              <h3>Demo 2 · Calculadora de ahorro</h3>
              <p>Traduce tiempo perdido en dinero real.</p>
              <div className="slider-group">
                <label>
                  Horas semanales perdidas: <strong>{hoursWeek}</strong>
                  <input type="range" min="1" max="40" value={hoursWeek} onChange={(e) => setHoursWeek(Number(e.target.value))} />
                </label>
                <label>
                  Coste hora (€): <strong>{costHour}</strong>
                  <input type="range" min="10" max="80" value={costHour} onChange={(e) => setCostHour(Number(e.target.value))} />
                </label>
                <label>
                  Personas implicadas: <strong>{people}</strong>
                  <input type="range" min="1" max="10" value={people} onChange={(e) => setPeople(Number(e.target.value))} />
                </label>
              </div>
              <div className="result-box">
                <strong>{Math.round(savings.monthlyCost)} € / mes</strong>
                <p>Coste estimado actual del proceso manual</p>
                <span>Ahorro potencial: {Math.round(savings.estimatedSavings)} € / mes · ROI estimado: {savings.roiMonths} meses</span>
              </div>
            </article>

            <article className="card demo-card">
              <h3>Demo 3 · Flujo visual</h3>
              <p>Explica el sistema de forma clara y comercial.</p>
              <div className="mini-flow">
                <div>Formulario</div>
                <span>→</span>
                <div>Clasificación</div>
                <span>→</span>
                <div>Respuesta</div>
                <span>→</span>
                <div>Agenda</div>
                <span>→</span>
                <div>Seguimiento</div>
              </div>
              <ul className="feature-list">
                <li>Entrada de lead ordenada</li>
                <li>Priorización automática</li>
                <li>Menos llamadas y menos pérdidas</li>
                <li>Más claridad operativa</li>
              </ul>
            </article>

            <article className="card demo-card">
              <h3>Demo 4 · Propuesta preliminar</h3>
              <p>Genera una orientación inicial de proyecto.</p>
              <div className="form-grid compact">
                <label>
                  Empresa
                  <input value={proposal.company} onChange={(e) => setProposal({ ...proposal, company: e.target.value })} placeholder="Nombre de la empresa" />
                </label>
                <label>
                  Sector
                  <select value={proposal.sector} onChange={(e) => setProposal({ ...proposal, sector: e.target.value })}>
                    <option>Servicios</option>
                    <option>Construcción</option>
                    <option>Administración</option>
                    <option>Otro</option>
                  </select>
                </label>
                <label>
                  Necesidad
                  <select value={proposal.need} onChange={(e) => setProposal({ ...proposal, need: e.target.value })}>
                    <option>Automatización de procesos</option>
                    <option>Captación digital</option>
                    <option>Asistente IA</option>
                    <option>Control interno</option>
                  </select>
                </label>
                <label>
                  Urgencia
                  <select value={proposal.urgency} onChange={(e) => setProposal({ ...proposal, urgency: e.target.value })}>
                    <option>Alta</option>
                    <option>Media</option>
                    <option>Baja</option>
                  </select>
                </label>
              </div>
              <div className="result-box">
                <strong>Recomendación inicial</strong>
                <p>{proposalText}</p>
                <span>Fases: análisis → diseño → implementación → pruebas.</span>
              </div>
            </article>
          </div>
        </div>
      </section>

      <section id="casos" className="section">
        <div className="container">
          <div className="section-title center">
            <span className="eyebrow">Casos de uso</span>
            <h2>Aplicaciones reales por sector</h2>
            <p>No se trata de meter tecnología porque sí. Se trata de resolver problemas concretos con un sistema claro.</p>
          </div>
          <div className="grid cols-3">
            {cases.map((item) => (
              <article key={item.title} className="card service-card">
                <h3>{item.title}</h3>
                <p>{item.text}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="section muted">
        <div className="container">
          <div className="section-title center">
            <span className="eyebrow">Método</span>
            <h2>Cómo trabajamos</h2>
            <p>El objetivo no es vender una herramienta. Es implantar un sistema que tenga sentido para el negocio.</p>
          </div>
          <div className="grid cols-4">
            {processSteps.map((step, index) => (
              <div key={step} className="card step-card">
                <span className="step-number">0{index + 1}</span>
                <h3>{step}</h3>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section id="propuesta" className="section cta-band">
        <div className="container cta-wrap">
          <div>
            <span className="eyebrow light">Propuesta</span>
            <h2>Solicita una propuesta adaptada a tu empresa</h2>
            <p>Cuéntame qué problema quieres resolver y preparo una orientación inicial seria, sin rodeos.</p>
          </div>
          <a href="#contacto" className="btn btn-light">Solicitar propuesta</a>
        </div>
      </section>

      <section id="contacto" className="section">
        <div className="container contact-grid">
          <div>
            <div className="section-title">
              <span className="eyebrow">Contacto</span>
              <h2>Hablemos de tu proyecto</h2>
              <p>Esta web ya está lista para presentar tu servicio. El siguiente paso es conectar el formulario al correo o CRM y subirla a producción.</p>
            </div>
            <div className="contact-cards">
              <div className="card soft-card"><strong>Email</strong><span>hola@pratsystems.es</span></div>
              <div className="card soft-card"><strong>Web</strong><span>pratsystems.es</span></div>
              <div className="card soft-card"><strong>Disponibilidad</strong><span>Reunión inicial y propuesta guiada</span></div>
            </div>
          </div>

          <form className="card form-card" onSubmit={(e) => e.preventDefault()}>
            <label>
              Nombre
              <input placeholder="Tu nombre" />
            </label>
            <label>
              Empresa
              <input placeholder="Nombre de la empresa" />
            </label>
            <label>
              Email
              <input type="email" placeholder="correo@empresa.com" />
            </label>
            <label>
              Mensaje
              <textarea placeholder="Cuéntame qué quieres automatizar o mejorar" rows={5} />
            </label>
            <button className="btn btn-primary" type="submit">Enviar solicitud</button>
            <small>Formulario visual listo. Falta conectarlo al correo, CRM o automatización.</small>
          </form>
        </div>
      </section>

      <footer className="footer">
        <div className="container footer-wrap">
          <div>
            <strong>PRAT SYSTEMS</strong>
            <p>Automatización y desarrollo de sistemas digitales.</p>
          </div>
          <div className="footer-links">
            <a href="#servicios">Servicios</a>
            <a href="#demos">Demos</a>
            <a href="#casos">Casos</a>
            <a href="#contacto">Contacto</a>
          </div>
        </div>
      </footer>
    </main>
  );
}
