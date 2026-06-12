import { Link } from 'react-router-dom';
import { Play } from 'lucide-react';

const Hero = () => {
  return (
    <div className="flex flex-col md:flex-row min-h-screen w-full overflow-hidden bg-white">
      {/* Left Side: Branding and Links */}
      <div className="flex-1 flex flex-col justify-center px-12 md:px-24 py-12 relative">
        <div className="max-w-xl">
          <div className="mb-16">
            <h1 className="text-7xl md:text-9xl font-black tracking-tighter leading-none">
              <span className="block text-[#ef6c00]">FCNami</span>
              <span className="block text-[#ff8c00]">T_T</span>
            </h1>
          </div>

          <nav className="flex flex-col gap-6 mb-20">
            <Link to="/" className="text-2xl font-bold text-slate-800 hover:text-[#ff8c00] transition-colors tracking-tight flex items-center gap-3 group">
              <span className="w-8 h-[2px] bg-slate-200 group-hover:bg-[#ff8c00] transition-all group-hover:w-12"></span>
              Home
            </Link>
            <Link to="/songs" className="text-2xl font-bold text-slate-800 hover:text-[#ff8c00] transition-colors tracking-tight flex items-center gap-3 group">
              <span className="w-8 h-[2px] bg-slate-200 group-hover:bg-[#ff8c00] transition-all group-hover:w-12"></span>
              Music & Lyrics
            </Link>
            <Link to="/request" className="text-2xl font-bold text-slate-800 hover:text-[#ff8c00] transition-colors tracking-tight flex items-center gap-3 group">
              <span className="w-8 h-[2px] bg-slate-200 group-hover:bg-[#ff8c00] transition-all group-hover:w-12"></span>
              Song Request
            </Link>
            <Link to="/queue" className="text-2xl font-bold text-slate-800 hover:text-[#ff8c00] transition-colors tracking-tight flex items-center gap-3 group">
              <span className="w-8 h-[2px] bg-slate-200 group-hover:bg-[#ff8c00] transition-all group-hover:w-12"></span>
              Live Queue
            </Link>
            <Link to="/about" className="text-2xl font-bold text-slate-800 hover:text-[#ff8c00] transition-colors tracking-tight flex items-center gap-3 group">
              <span className="w-8 h-[2px] bg-slate-200 group-hover:bg-[#ff8c00] transition-all group-hover:w-12"></span>
              About
            </Link>
          </nav>

          <div className="flex items-center gap-6">
            <a 
              href="https://www.youtube.com/@fcnamit_t" 
              target="_blank" 
              rel="noopener noreferrer"
              className="flex items-center gap-4 group"
            >
              <div className="w-14 h-14 bg-black rounded-full flex items-center justify-center text-white shadow-xl shadow-slate-200 group-hover:bg-slate-800 transition-all transform group-hover:scale-110">
                <Play size={28} fill="white" className="ml-1" />
              </div>
              <div>
                <span className="block text-sm font-bold text-slate-400 uppercase tracking-widest">Follow on</span>
                <span className="block text-xl font-black text-slate-900 uppercase">YouTube</span>
              </div>
            </a>
          </div>
        </div>

        {/* Decorative background number or text could go here if in reference */}
      </div>

      {/* Right Side: Image */}
      <div className="flex-1 relative h-[50vh] md:h-screen">
        <img 
          src="/assets/hero.jpg" 
          alt="FCNami T_T Character" 
          className="w-full h-full object-cover object-center"
        />
        {/* Subtle overlay to soften the image transition if needed */}
        <div className="absolute inset-0 bg-gradient-to-l from-transparent via-transparent to-white/10 md:to-white/20 pointer-events-none" />
      </div>
    </div>
  );
};

export default Hero;
