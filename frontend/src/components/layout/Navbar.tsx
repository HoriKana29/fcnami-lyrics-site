import { Link } from 'react-router-dom';
import { Home, Music, List, MessageCircle, Info, Youtube } from 'lucide-react';

const Navbar = () => {
  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white/80 backdrop-blur-md border-b border-slate-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-2">
            <Link to="/" className="flex items-center gap-2 group">
              <div className="w-10 h-10 bg-[#ff8c00] rounded-full flex items-center justify-center group-hover:bg-[#ffa500] transition-colors">
                <Music className="text-white w-6 h-6" />
              </div>
              <span className="text-xl font-bold text-slate-900 tracking-tight">FCNami <span className="text-[#ff8c00]">T_T</span></span>
            </Link>
          </div>

          <div className="hidden md:block">
            <div className="flex items-baseline space-x-6">
              <Link to="/" className="text-slate-600 hover:text-[#ff8c00] px-3 py-2 rounded-md text-sm font-bold transition-colors flex items-center gap-2">
                <Home size={18} /> Home
              </Link>
              <Link to="/songs" className="text-slate-600 hover:text-[#ff8c00] px-3 py-2 rounded-md text-sm font-bold transition-colors flex items-center gap-2">
                <Music size={18} /> Songs
              </Link>
              <Link to="/request" className="text-slate-600 hover:text-[#ff8c00] px-3 py-2 rounded-md text-sm font-bold transition-colors flex items-center gap-2">
                <MessageCircle size={18} /> Request
              </Link>
              <Link to="/queue" className="text-slate-600 hover:text-[#ff8c00] px-3 py-2 rounded-md text-sm font-bold transition-colors flex items-center gap-2">
                <List size={18} /> Queue
              </Link>
              <Link to="/about" className="text-slate-600 hover:text-[#ff8c00] px-3 py-2 rounded-md text-sm font-bold transition-colors flex items-center gap-2">
                <Info size={18} /> About
              </Link>
            </div>
          </div>

          <div className="flex items-center gap-4">
            <a 
              href="https://www.youtube.com/@fcnamit_t" 
              target="_blank" 
              rel="noopener noreferrer"
              className="bg-black hover:bg-slate-800 text-white px-4 py-2 rounded-full text-sm font-bold flex items-center gap-2 transition-all hover:scale-105 active:scale-95 shadow-lg shadow-slate-200"
            >
              <Youtube size={18} /> YouTube
            </a>
          </div>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
