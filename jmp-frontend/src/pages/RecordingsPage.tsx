import { Box, Typography, Card, CardContent } from '@mui/material';

export default function RecordingsPage() {
  return (
    <Box>
      <Typography variant="h4" fontWeight="bold" gutterBottom>
        Recordings
      </Typography>
      <Card>
        <CardContent>
          <Typography variant="body1">
            Recordings management interface - Coming soon
          </Typography>
          <Typography variant="body2" color="text.secondary" mt={2}>
            This page will display conference recordings with playback, 
            download, and deletion capabilities.
          </Typography>
        </CardContent>
      </Card>
    </Box>
  );
}
