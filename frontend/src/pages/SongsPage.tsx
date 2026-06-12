import { Link } from 'react-router-dom';
import { ChevronRight, TrendingUp, Clock, MessageSquare, Gamepad2, Star } from 'lucide-react';
import Navbar from '@/components/layout/Navbar';

interface Category {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  path: string;
  color: string;
}

const categories: Category[] = [
  {
    id: 'popular',
    title: 'Popular Songs',
    description: 'Most viewed translations, sorted by community preference.',
    icon: <TrendingUp size={28} />,
    path: '/songs/search?sort=views',
    color: 'bg-orange-500',
  },
  {
    id: 'recommend',
    title: 'Recommend',
    description: "Curated selection of our best translations you shouldn't miss.",
    icon: <Star size={28} />,
    path: '/songs/search?playlist=recommend',
    color: 'bg-amber-400',
  },
  {
    id: 'long-track',
    title: 'Long Track',
    description: 'Long Play Japanese Tracks (40+ minutes) for your study or relaxation.',
    icon: <Clock size={28} />,
    path: '/songs/search?playlist=long-play',
    color: 'bg-blue-500',
  },
  {
    id: 'requested',
    title: 'Requested Song',
    description: 'Songs requested by our wonderful viewers on YouTube.',
    icon: <MessageSquare size={28} />,
    path: '/songs/search?playlist=requested',
    color: 'bg-pink-500',
  },
  {
    id: 'bandori',
    title: 'Bandori Music',
    description: 'High-energy rhythm game tracks from the Bandori universe.',
    icon: <Gamepad2 size={28} />,
    path: '/songs/search?playlist=bandori',
    color: 'bg-violet-500',
  },
];

const SongsPage = () => {
  return (
    <div className="bg-white min-h-screen">
      <Navbar />
      
      <div className="flex flex-col md:flex-row min-h-screen pt-16">
        {/* Left Side: Category Menu */}
        <div className="flex-1 px-8 md:px-20 py-12 flex flex-col justify-center">
          <header className="mb-16">
            <h1 className="text-6xl md:text-8xl font-black tracking-tighter leading-none mb-6">
              <span className="block text-[#ef6c00]">Music</span>
              <span className="block text-[#ff8c00]">& Lyrics</span>
            </h1>
            <p className="text-slate-500 text-lg max-w-md font-medium">
              Select a category to explore our collection of high-quality Japanese song translations.
            </p>
          </header>

          {/* Categories List */}
          <div className="flex flex-col gap-6 max-w-2xl">
            {categories.map((category) => (
              <Link
                key={category.id}
                to={category.path}
                className="flex items-center justify-between p-8 bg-white hover:bg-slate-50 border-2 border-slate-100 hover:border-[#ff8c00]/30 rounded-[2rem] transition-all group shadow-sm hover:shadow-md"
              >
                <div className="flex items-center gap-6">
                  <div className={`w-16 h-16 ${category.color} rounded-2xl flex items-center justify-center text-white shadow-lg transform group-hover:rotate-3 transition-transform`}>
                    {category.icon}
                  </div>
                  <div>
                    <h3 className="text-2xl font-black text-slate-900 leading-tight group-hover:text-[#ef6c00] transition-colors">
                      {category.title}
                    </h3>
                    <p className="text-slate-400 font-bold text-sm mt-1 max-w-xs">
                      {category.description}
                    </p>
                  </div>
                </div>
                <div className="w-12 h-12 rounded-full border-2 border-slate-100 flex items-center justify-center text-slate-200 group-hover:border-[#ff8c00] group-hover:text-[#ff8c00] transition-all">
                  <ChevronRight size={24} className="group-hover:translate-x-1 transition-transform" />
                </div>
              </Link>
            ))}
          </div>
        </div>

        {/* Right Side: Fixed Image */}
        <div className="md:w-[45%] relative h-[40vh] md:h-screen">
          <div className="sticky top-0 h-full w-full overflow-hidden">
            <img 
              src="/assets/songs-hero.jpg" 
              alt="Songs Category Visual" 
              className="w-full h-full object-cover object-center"
            />
            {/* Subtle overlay to soften the image transition */}
            <div className="absolute inset-0 bg-gradient-to-l from-transparent via-transparent to-white/10 pointer-events-none" />
          </div>
        </div>
      </div>
    </div>
  );
};

export default SongsPage;
