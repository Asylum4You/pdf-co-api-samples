//*******************************************************************************************//
//                                                                                           //
// Download Free Evaluation Version From: https://bytescout.com/download/web-installer       //
//                                                                                           //
// Also available as Web API! Get Your Free API Key: https://app.pdf.co/signup               //
//                                                                                           //
// Copyright © 2017-2020 ByteScout, Inc. All rights reserved.                                //
// https://www.bytescout.com                                                                 //
// https://pdf.co                                                                            //
//                                                                                           //
//*******************************************************************************************//


package com.company;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main
{
    // The authentication key (API Key).
    // Get your own by registering at https://app.pdf.co/documentation/api
    final static String API_KEY = "***********************************";

    // Direct URL of source PDF file.
    final static String SourceFileUrl = "https://bytescout-com.s3.amazonaws.com/files/demo-files/cloud-api/pdf-to-csv/sample.pdf";
    // Comma-separated list of page indices (or ranges) to process. Leave empty for all pages. Example: '0,2-5,7-'.
    final static String Pages = "";
    // PDF document password. Leave empty for unprotected documents.
    final static String Password = "";
    // Destination CSV file name
    final static Path DestinationFile = Paths.get(".\\result.csv");

	/*
	Some of advanced options available through profiles:
	(JSON can be single/double-quoted and contain comments.)
	{
		"profiles": [
			{
				"profile1": {
					"CSVSeparatorSymbol": ",", // Separator symbol.
					"CSVQuotaionSymbol": "\"", // Quotation symbol.
					"ExtractInvisibleText": true, // Invisible text extraction. Values: true / false
					"ExtractShadowLikeText": true, // Shadow-like text extraction. Values: true / false
					"LineGroupingMode": "None", // Values: "None", "GroupByRows", "GroupByColumns", "JoinOrphanedRows"
					"ColumnDetectionMode": "ContentGroupsAndBorders", // Values: "ContentGroupsAndBorders", "ContentGroups", "Borders", "BorderedTables"
					"Unwrap": false, // Unwrap grouped text in table cells. Values: true / false
					"ShrinkMultipleSpaces": false, // Shrink multiple spaces in table cells that affect column detection. Values: true / false
					"DetectNewColumnBySpacesRatio": 1, // Spacing ratio that affects column detection.
					"CustomExtractionColumns": [ 0, 50, 150, 200, 250, 300 ], // Explicitly specify columns coordinates for table extraction.
					"CheckPermissions": true, // Ignore document permissions. Values: true / false
				}
			}
		]
	}
	*/

    // Advanced Conversation Options
    final static String Profiles = "{ 'profiles': [ { 'profile1': { 'TextFromImagesAndVectorsAndFonts': '|' } } ] }";


    public static void main(String[] args) throws IOException
    {
        // Create HTTP client instance
        OkHttpClient webClient = new OkHttpClient();

        // Prepare URL for `PDF To CSV` API call
        String query = String.format(
                "https://api.pdf.co/v1/pdf/convert/to/csv?name=%s&password=%s&pages=%s&url=%s&profiles=%s",
                DestinationFile.getFileName(),
                Password,
                Pages,
                SourceFileUrl,
                Profiles);

        // Make correctly escaped (encoded) URL
        URL url = null;
        try
        {
            url = new URI(null, query, null).toURL();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }

        // Prepare request
        Request request = new Request.Builder()
                .url(url)
                .addHeader("x-api-key", API_KEY) // (!) Set API Key
                .build();

        // Execute request
        Response response = webClient.newCall(request).execute();

        if (response.code() == 200)
        {
            // Parse JSON response
            JsonObject json = new JsonParser().parse(response.body().string()).getAsJsonObject();

            String status = json.get("status").getAsString();
            if (!status.equals("error"))
            {
                // Get URL of generated CSV file
                String resultFileUrl = json.get("url").getAsString();

                // Download CSV file
                downloadFile(webClient, resultFileUrl, DestinationFile.toFile());

                System.out.printf("Generated CSV file saved as \"%s\" file.", DestinationFile.toString());
            }
            else
            {
                // Display service reported error
                System.out.println(json.get("message").getAsString());
            }
        }
        else
        {
            // Display request error
            System.out.println(response.code() + " " + response.message());
        }
    }

    public static void downloadFile(OkHttpClient webClient, String url, File destinationFile) throws IOException
    {
        // Prepare request
        Request request = new Request.Builder()
                .url(url)
                .build();
        // Execute request
        Response response = webClient.newCall(request).execute();

        byte[] fileBytes = response.body().bytes();

        // Save downloaded bytes to file
        OutputStream output = new FileOutputStream(destinationFile);
        output.write(fileBytes);
        output.flush();
        output.close();

        response.close();
    }
}
