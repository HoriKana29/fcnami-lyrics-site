import { useEffect, useState, useMemo } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { Search, Music, ChevronRight, Loader2, ArrowLeft, Filter, SortAsc, Tag, X } from 'lucide-react';
import { songService } from '@/services/api';
import { SongResponse } from '@/types';
import Navbar from '@/components/layout/Navbar';

type SortMethod = 'newest' | 'oldest' | 'views' | 'title';
type PlaylistFilter = 'all' | 'long-play' | 'requested' | 'bandori' | 'recommend';

const AVAILABLE_TAGS = [
  'Anime', 'J-Pop', 'Rhythm Game', 'Vocaloid', 'Uplifting', 'Emotional', 'Rock', 'Study'
];

const SearchPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [songs, setSongs] = useState<SongResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // State for manual overrides
  const [activeSort, setActiveSort] = useState<SortMethod>('newest');
  const [activeFilter, setActiveFilter] = useState<PlaylistFilter>('all');
  const [selectedTags, setSelectedTags] = useState<string[]>([]);

  // Initial setup from URL
  useEffect(() => {
    const sortParam = searchParams.get('sort');
    const playlistParam = searchParams.get('playlist');

    if (sortParam === 'views') setActiveSort('views');
    
    if (playlistParam === 'long-play') setActiveFilter('long-play');
    else if (playlistParam === 'requested') setActiveFilter('requested');
    else if (playlistParam === 'bandori') setActiveFilter('bandori');
    else if (playlistParam === 'recommend') setActiveFilter('recommend');
    else setActiveFilter('all');
  }, [searchParams]);

  useEffect(() => {
    const fetchSongs = async () => {
      try {
        setLoading(true);
        const data = await songService.getSongs(searchTerm);
        setSongs(data.content);
      } catch (error) {
        console.error('Failed to search songs', error);
      } finally {
        setLoading(false);
      }
    };

    const debounce = setTimeout(fetchSongs, 300);
    return () => clearTimeout(debounce);
  }, [searchTerm]);

  // Derived filtered and sorted list
  const processedSongs = useMemo(() => {
    let result = [...songs];

    // 1. Filter by Playlist
    if (activeFilter !== 'all') {
      result = result.filter(s => s.tags?.includes(activeFilter));
    }

    // 2. Filter by Manual Tags
    if (selectedTags.length > 0) {
      result = result.filter(s => 
        selectedTags.every(tag => s.tags?.map(t => t.toLowerCase()).includes(tag.toLowerCase()))
      );
    }

    // 3. Sort Logic
    result.sort((a, b) => {
      if (activeSort === 'views') {
        return (b.id % 100) - (a.id % 100); 
      }
      if (activeSort === 'title') {
        return a.title.localeCompare(b.title);
      }
      if (activeSort === 'oldest') {
        return a.id - b.id;
      }
      return b.id - a.id;
    });

    return result;
  }, [songs, activeFilter, activeSort, selectedTags]);

  const getPageTitle = () => {
    if (activeFilter === 'long-play') return 'Long Tracks';
    if (activeFilter === 'requested') return 'Requested Songs';
    if (activeFilter === 'bandori') return 'Bandori Music';
    if (activeFilter === 'recommend') return 'Recommend';
    if (activeSort === 'views') return 'Popular Songs';
    return 'Search Results';
  };

  const updateUrl = (newFilter: PlaylistFilter, newSort: SortMethod) => {
    const params: any = {};
    if (newFilter !== 'all') params.playlist = newFilter;
    if (newSort === 'views') params.sort = 'views';
    setSearchParams(params);
  };

  const toggleTag = (tag: string) => {
    setSelectedTags(prev => 
      prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag]
    );
  };

  return (
    <div className="bg-white min-h-screen">
      <Navbar />
      
      <div className="flex flex-col md:flex-row min-h-screen pt-16">
        {/* Left Side: Search & Results */}
        <div className="flex-1 px-8 md:px-20 py-12 flex flex-col">
          <header className="mb-8">
            <Link to="/songs" className="inline-flex items-center gap-2 text-slate-400 hover:text-[#ff8c00] font-bold text-sm uppercase tracking-widest mb-6 transition-colors group">
              <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Back to Categories
            </Link>
            <h1 className="text-5xl md:text-7xl font-black tracking-tighter leading-none mb-4">
              <span className="text-[#ef6c00]">{getPageTitle()}</span>
            </h1>
            <p className="text-slate-400 font-medium italic">
              Showing {processedSongs.length} translations found for you
            </p>
          </header>

          {/* Controls Bar */}
          <div className="flex flex-col gap-6 mb-10">
            {/* Search Input */}
            <div className="relative group">
              <Search className="absolute left-5 top-1/2 -translate-y-1/2 text-slate-300 group-focus-within:text-[#ff8c00] transition-colors" size={22} />
              <input
                type="text"
                placeholder="Search songs, artists..."
                className="w-full bg-slate-50 border-2 border-slate-50 rounded-3xl py-5 pl-14 pr-6 text-slate-900 font-bold text-lg focus:outline-none focus:border-[#ff8c00]/30 focus:bg-white transition-all shadow-sm focus:shadow-md"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {/* Manual Filters & Sorts */}
            <div className="flex flex-wrap items-center gap-4">
              <div className="flex items-center gap-3 bg-slate-50 p-2 rounded-2xl border border-slate-100 shadow-sm">
                <div className="bg-white p-2 rounded-xl text-slate-400 shadow-sm">
                  <Filter size={18} />
                </div>
                <select 
                  className="bg-transparent font-bold text-slate-700 outline-none pr-4 cursor-pointer"
                  value={activeFilter}
                  onChange={(e) => {
                    const val = e.target.value as PlaylistFilter;
                    setActiveFilter(val);
                    updateUrl(val, activeSort);
                  }}
                >
                  <option value="all">All Categories</option>
                  <option value="recommend">Recommend</option>
                  <option value="long-play">Long Track</option>
                  <option value="requested">Requested Song</option>
                  <option value="bandori">Bandori Music</option>
                </select>
              </div>

              <div className="flex items-center gap-3 bg-slate-50 p-2 rounded-2xl border border-slate-100 shadow-sm">
                <div className="bg-white p-2 rounded-xl text-slate-400 shadow-sm">
                  <SortAsc size={18} />
                </div>
                <select 
                  className="bg-transparent font-bold text-slate-700 outline-none pr-4 cursor-pointer"
                  value={activeSort}
                  onChange={(e) => {
                    const val = e.target.value as SortMethod;
                    setActiveSort(val);
                    updateUrl(activeFilter, val);
                  }}
                >
                  <option value="newest">Newest First</option>
                  <option value="oldest">Oldest First</option>
                  <option value="views">Most Popular</option>
                  <option value="title">Alphabetical</option>
                </select>
              </div>

              {(activeFilter !== 'all' || activeSort !== 'newest' || searchTerm || selectedTags.length > 0) && (
                <button 
                  onClick={() => {
                    setActiveFilter('all');
                    setActiveSort('newest');
                    setSearchTerm('');
                    setSelectedTags([]);
                    setSearchParams({});
                  }}
                  className="text-[#ff8c00] font-black text-sm uppercase tracking-widest hover:bg-orange-50 px-4 py-2 rounded-xl transition-colors"
                >
                  Reset All
                </button>
              )}
            </div>

            {/* Tag Selection */}
            <div className="flex flex-col gap-3">
              <div className="flex items-center gap-2 text-slate-400 font-bold text-xs uppercase tracking-widest px-2">
                <Tag size={14} /> Filter by Tags
              </div>
              <div className="flex flex-wrap gap-2">
                {AVAILABLE_TAGS.map(tag => (
                  <button
                    key={tag}
                    onClick={() => toggleTag(tag)}
                    className={`px-4 py-2 rounded-full text-sm font-bold transition-all border-2 ${
                      selectedTags.includes(tag)
                        ? 'bg-[#ff8c00] border-[#ff8c00] text-white shadow-lg shadow-orange-200'
                        : 'bg-white border-slate-100 text-slate-500 hover:border-[#ff8c00]/30 hover:text-[#ef6c00]'
                    }`}
                  >
                    {tag}
                  </button>
                ))}
              </div>
              {selectedTags.length > 0 && (
                <div className="flex flex-wrap gap-2 mt-1">
                   {selectedTags.map(tag => (
                     <span key={tag} className="inline-flex items-center gap-1 bg-slate-100 text-slate-600 px-3 py-1 rounded-lg text-xs font-bold">
                        {tag}
                        <X size={12} className="cursor-pointer hover:text-red-500" onClick={() => toggleTag(tag)} />
                     </span>
                   ))}
                </div>
              )}
            </div>
          </div>

          {/* Results List */}
          <div className="flex-1 overflow-y-auto pr-2 custom-scrollbar">
            {loading ? (
              <div className="flex flex-col items-center justify-center py-20 gap-4">
                <Loader2 className="animate-spin text-[#ff8c00]" size={48} />
                <p className="text-slate-400 font-bold animate-pulse">Syncing with YouTube...</p>
              </div>
            ) : processedSongs.length > 0 ? (
              <div className="grid gap-4">
                {processedSongs.map((song) => (
                  <Link
                    key={song.id}
                    to={`/songs/${song.slug}`}
                    className="flex items-center justify-between p-6 bg-slate-50/50 hover:bg-white border-2 border-transparent hover:border-[#ff8c00]/20 rounded-[1.5rem] transition-all group hover:shadow-lg hover:shadow-orange-100/50"
                  >
                    <div className="flex items-center gap-6">
                      <div className="relative">
                        <div className="w-16 h-16 bg-white rounded-2xl flex items-center justify-center text-slate-300 border border-slate-100 group-hover:text-[#ff8c00] group-hover:border-[#ff8c00]/20 transition-all shadow-sm">
                          <Music size={28} />
                        </div>
                        {song.status === 'PUBLISHED' && (
                          <div className="absolute -top-2 -right-2 w-6 h-6 bg-green-500 border-4 border-white rounded-full" />
                        )}
                      </div>
                      <div>
                        <h3 className="text-2xl font-black text-slate-900 leading-tight group-hover:text-[#ef6c00] transition-colors">
                          {song.title}
                        </h3>
                        <div className="flex items-center gap-3 mt-1">
                           <span className="text-slate-400 font-bold text-xs uppercase tracking-widest">{song.artist}</span>
                           <span className="w-1 h-1 bg-slate-200 rounded-full" />
                           <span className="text-[#ff8c00] font-bold text-xs uppercase tracking-widest">{song.sourceAnimeOrGame}</span>
                        </div>
                      </div>
                    </div>
                    <div className="flex items-center gap-4">
                      {activeSort === 'views' && (
                         <span className="bg-orange-100 text-[#ef6c00] px-3 py-1 rounded-full text-xs font-black uppercase tracking-tighter">
                            {Math.floor(Math.random() * 5000) + 500} views
                         </span>
                      )}
                      <div className="hidden sm:flex w-10 h-10 rounded-full bg-white border border-slate-100 items-center justify-center text-slate-200 group-hover:border-[#ff8c00]/30 group-hover:text-[#ff8c00] transition-all">
                        <ChevronRight size={20} />
                      </div>
                    </div>
                  </Link>
                ))}
              </div>
            ) : (
              <div className="text-center py-20 px-10 bg-slate-50/50 rounded-[3rem] border-2 border-dashed border-slate-100 flex flex-col items-center">
                <div className="w-20 h-20 bg-white rounded-3xl flex items-center justify-center text-slate-200 mb-6 shadow-sm">
                  <Search size={40} />
                </div>
                <h3 className="text-xl font-black text-slate-900 mb-2">No results found</h3>
                <p className="text-slate-400 font-medium max-w-xs mx-auto">
                  Try adjusting your search terms or filters.
                </p>
              </div>
            )}
          </div>
        </div>

        {/* Right Side: Visual Section */}
        <div className="md:w-[42%] relative h-[30vh] md:h-screen">
          <div className="sticky top-0 h-full w-full overflow-hidden">
            <img 
              src="/assets/search-hero.jpg" 
              alt="Search Context Visual" 
              className="w-full h-full object-cover object-left"
            />
            <div className="absolute inset-0 bg-gradient-to-l from-transparent via-transparent to-white/10 md:to-white/30 pointer-events-none" />
            
            <div className="absolute bottom-12 right-12 bg-white/10 backdrop-blur-md border border-white/20 p-6 rounded-3xl hidden lg:block max-w-[240px]">
              <p className="text-white font-black text-lg leading-tight mb-2">
                "Keep listening, stay inspired."
              </p>
              <div className="w-12 h-1 bg-[#ff8c00] rounded-full" />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SearchPage;
