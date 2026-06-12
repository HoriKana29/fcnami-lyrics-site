import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import HomePage from '@/pages/HomePage';
import SongsPage from '@/pages/SongsPage';
import SearchPage from '@/pages/SearchPage';
import SongDetailPage from '@/pages/SongDetailPage';

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/songs" element={<SongsPage />} />
        <Route path="/songs/search" element={<SearchPage />} />
        <Route path="/songs/:slug" element={<SongDetailPage />} />
        {/* Fallback for other routes while they are being built */}
        <Route path="*" element={<HomePage />} />
      </Routes>
    </Router>
  );
}

export default App;
