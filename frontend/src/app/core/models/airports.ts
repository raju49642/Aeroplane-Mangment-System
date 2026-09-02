// Shared airport data with both long and short forms
export interface Airport {
  city: string;
  code: string;
  shortAlias: readonly string[];
}

export const AIRPORTS_DATA: readonly Airport[] = [
  { city: 'Delhi', code: 'DEL', shortAlias: ['del', 'delhi', 'dilli'] },
  { city: 'Mumbai', code: 'BOM', shortAlias: ['bom', 'mumbai'] },
  { city: 'Bangalore', code: 'BLR', shortAlias: ['blr', 'bangalore', 'bengaluru'] },
  { city: 'Chennai', code: 'MAA', shortAlias: ['maa', 'chennai'] },
  { city: 'Kolkata', code: 'CCU', shortAlias: ['ccu', 'kolkata', 'calcutta'] },
  { city: 'Pune', code: 'PNQ', shortAlias: ['pnq', 'pune'] },
  { city: 'Hyderabad', code: 'HYD', shortAlias: ['hyd', 'hyderabad'] },
  { city: 'Kochi', code: 'COK', shortAlias: ['cok', 'kochi', 'cochin'] },
  { city: 'Chandigarh', code: 'IXC', shortAlias: ['ixc', 'chandigarh'] },
  { city: 'Jaipur', code: 'JAI', shortAlias: ['jai', 'jaipur'] },
  { city: 'Ahmedabad', code: 'AMD', shortAlias: ['amd', 'ahmedabad', 'ahmednagar'] },
  { city: 'Lucknow', code: 'LKO', shortAlias: ['lko', 'lucknow'] },
  { city: 'Indore', code: 'IDR', shortAlias: ['idr', 'indore'] },
  { city: 'Goa', code: 'GOI', shortAlias: ['goi', 'goa'] },
  { city: 'Srinagar', code: 'SXR', shortAlias: ['sxr', 'srinagar'] }
];

// Get list of city names only
export const CITY_NAMES = AIRPORTS_DATA.map(a => a.city);

// Search function: accepts both long and short forms
export function searchAirport(query: string): string | null {
  if (!query) return null;
  const searchTerm = query.toLowerCase().trim();
  
  const result = AIRPORTS_DATA.find(airport => 
    airport.shortAlias.includes(searchTerm) || 
    airport.city.toLowerCase() === searchTerm ||
    airport.code.toLowerCase() === searchTerm
  );
  
  return result ? result.city : null;
}

// Validate if input matches an airport (returns normalized city name)
export function normalizeAirport(input: string): string | null {
  return searchAirport(input);
}

// Get code by city name
export function getCityCode(cityName: string): string | null {
  const airport = AIRPORTS_DATA.find(a => a.city === cityName);
  return airport ? airport.code : null;
}
