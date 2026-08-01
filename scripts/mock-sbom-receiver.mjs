// A stand-in for whatever consumes the SBOM in a real setup — Dependency-Track, an internal
// inventory service, a compliance mailbox. It exists so the upload half of the SBOM pipeline can be
// run and seen rather than only described, which is the difference between a pipeline that works and
// one that has never been executed.
//
//   node scripts/mock-sbom-receiver.mjs &
//   ./gradlew :sdk:cyclonedxDirectBom
//   curl -X POST http://localhost:8787/sbom \
//     -H "Authorization: Bearer local-token" \
//     -H "Content-Type: application/vnd.cyclonedx+json" \
//     --data-binary @sdk/build/reports/cyclonedx-direct/bom.json
//
// Node's standard library only, so there is nothing to install before running it.

import { createServer } from 'node:http'

const port = Number(process.env.PORT ?? 8787)
const expectedToken = process.env.SBOM_UPLOAD_TOKEN ?? 'local-token'

const respond = (response, status, body) => {
  response.writeHead(status, { 'Content-Type': 'application/json' })
  response.end(JSON.stringify(body))
}

createServer((request, response) => {
  if (request.method !== 'POST') {
    return respond(response, 405, { error: 'only POST is accepted' })
  }

  // Rejecting a missing or wrong token is the whole point of exercising this locally: it is the
  // failure a real receiver produces, and the one a pipeline should fail on rather than skip past.
  if (request.headers.authorization !== `Bearer ${expectedToken}`) {
    console.error('rejected an upload with a missing or incorrect token')
    return respond(response, 401, { error: 'unauthorized' })
  }

  const chunks = []
  request.on('data', (chunk) => chunks.push(chunk))
  request.on('end', () => {
    const raw = Buffer.concat(chunks).toString('utf8')

    let document
    try {
      document = JSON.parse(raw)
    } catch {
      console.error('rejected an upload that was not valid JSON')
      return respond(response, 400, { error: 'body is not valid JSON' })
    }

    const component = document.metadata?.component
    console.log(
      [
        `accepted ${component?.name ?? 'unknown'}@${component?.version ?? 'unknown'}`,
        `spec ${document.specVersion ?? 'unknown'}`,
        `${document.components?.length ?? 0} components`,
        `${(raw.length / 1024).toFixed(1)} KiB`,
      ].join(', '),
    )

    respond(response, 202, { received: document.components?.length ?? 0 })
  })
}).listen(port, () => {
  console.log(`mock SBOM receiver listening on http://localhost:${port} (token: ${expectedToken})`)
})
