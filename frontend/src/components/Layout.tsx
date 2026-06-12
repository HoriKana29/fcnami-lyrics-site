import { ReactNode } from 'react';
import { NavLink } from 'react-router-dom';
import { ArrowRight, Youtube } from 'lucide-react';

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <div className='min-h-screen bg-[#f7f7f4] text-[#202020] font-sans'>
      <header className='mx-auto flex w-[min(1700px,calc(100%-40px))] items-center justify-between gap-8 py-10'>
        <nav className='flex flex-wrap items-center gap-10 text-[clamp(1.1rem,2vw,2rem)] font-medium text-[#2a2a2d]'>
          <NavLink to='/' className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>Home</NavLink>
          <NavLink to='/songs' className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>Songs</NavLink>
          <NavLink to='/request' className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>Request</NavLink>
          <NavLink to='/queue' className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>Queue</NavLink>
        </nav>
        <a
          href='https://www.youtube.com/@FCNami_TT'
          className='pill-button min-w-[176px]'
          target='_blank'
          rel='noreferrer'
          aria-label='Open FCNami T_T on YouTube'
        >
          <Youtube size={24} />
          YouTube
          <ArrowRight size={24} />
        </a>
      </header>
      <main className='mx-auto w-[min(1700px,calc(100%-40px))]'>
        {children}
      </main>
    </div>
  );
}
