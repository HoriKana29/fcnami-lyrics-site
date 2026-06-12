import { Routes, Route } from 'react-router-dom';
import HomePage from './components/HomePage';
import SongsPage from './components/SongsPage';
import SearchPage from './components/SearchPage';
import LongTracksPage from './components/LongTracksPage';
import PlayerPage from './components/PlayerPage';
import RulesPage from './components/RulesPage';
import QueuePage from './components/QueuePage';
import AboutPage from './components/AboutPage';

export default function App() {
  return (
    <Routes>
      <Route path='/' element={<HomePage />} />
      <Route path='/songs' element={<SongsPage />} />
      <Route path='/songs/:slug' element={<PlayerPage />} />
      <Route path='/search' element={<SearchPage />} />
      <Route path='/long-tracks' element={<LongTracksPage />} />
      <Route path='/player' element={<PlayerPage />} />
      <Route path='/request' element={<RulesPage />} />
      <Route path='/queue' element={<QueuePage />} />
      <Route path='/about' element={<AboutPage />} />
      <Route path='*' element={<HomePage />} />
    </Routes>
  );
}
