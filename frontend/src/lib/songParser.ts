export interface ParsedTitle {
  title: string;
  artist: string;
  coveredBy?: string;
  source?: string;
}

/**
 * Parses a raw song title string into a structured object.
 * Logic mirrors the Backend's SongTitleParser.
 */
export function parseSongTitle(rawTitle: string): ParsedTitle {
  if (!rawTitle || rawTitle.trim() === '') {
    return { title: '', artist: '' };
  }

  let current = rawTitle;
  const result: Partial<ParsedTitle> = {};

  // 1. Extract Source [ Source ]
  const sourceMatch = current.match(/\[(.*?)\]/);
  if (sourceMatch) {
    result.source = sourceMatch[1].trim();
    current = current.replace(sourceMatch[0], '');
  }

  // 2. Extract Covered By
  // Match "ซับไทย Covered by ..." or "Covered by ..." (case insensitive)
  const coveredRegex = /(?:ซับไทย\s+)?covered\s+by\s+(.*)/i;
  const coveredMatch = current.match(coveredRegex);
  if (coveredMatch) {
    result.coveredBy = coveredMatch[1].trim();
    current = current.substring(0, coveredMatch.index).trim();
  } else {
    // Check for just "ซับไทย"
    const subThaiMatch = current.match(/ซับไทย.*/);
    if (subThaiMatch) {
      current = current.substring(0, subThaiMatch.index).trim();
    }
  }

  // 3. Split Title and Artist
  const parts = current.split(/\s*[/-]\s*/);
  if (parts.length >= 2) {
    result.title = parts[0].trim();
    result.artist = parts[1].trim();
  } else {
    result.title = current.trim();
    result.artist = 'FCNami T_T';
  }

  return {
    title: result.title || '',
    artist: result.artist || '',
    coveredBy: result.coveredBy,
    source: result.source,
  };
}
